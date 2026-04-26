using System.Collections.Concurrent;
using System.IdentityModel.Tokens.Jwt;
using FluentValidation;
using Identity.Application.DTOs;
using Identity.Application.Helpers;
using Identity.Application.Interfaces;
using Identity.Entities;
using Microsoft.EntityFrameworkCore;
using Npgsql;
using Shared.Auth;
using Shared.Errors;
using ValidationException = Shared.Errors.ValidationException;

namespace Identity.Application.Services
{
    public class AuthService : IAuthService
    {
        private readonly IAuthRepository _repository;
        private readonly IJwtTokenService _tokenService;
        private readonly IValidator<RegisterRequest> _registerValidator;
        private readonly IValidator<LoginRequest> _loginValidator;

        // In-memory blacklist of revoked JWT IDs (jti). For production use a distributed
        // store (Redis); for the course project an in-memory set is acceptable.
        private static readonly ConcurrentDictionary<string, DateTime> RevokedTokens = new();

        public AuthService(
            IAuthRepository repository,
            IJwtTokenService tokenService,
            IValidator<RegisterRequest> registerValidator,
            IValidator<LoginRequest> loginValidator)
        {
            _repository = repository;
            _tokenService = tokenService;
            _registerValidator = registerValidator;
            _loginValidator = loginValidator;
        }

        public async Task<RegisterResponse> RegisterAsync(RegisterRequest request, CancellationToken ct = default)
        {
            var result = await _registerValidator.ValidateAsync(request, ct);
            if (!result.IsValid)
            {
                var details = result.Errors
                    .Select(e => new ApiErrorDetail { Field = e.PropertyName, Message = e.ErrorMessage })
                    .ToList();
                throw new ValidationException("Invalid registration data", details);
            }

            if (await _repository.IsLoginTakenAsync(request.Username, ct))
                throw new ConflictException("USERNAME_TAKEN", "Username is already taken");

            var user = new User
            {
                UserId = Guid.NewGuid(),
                Login = request.Username,
                PasswordHash = PasswordHasher.HashPassword(request.Password),
                RegistrationDate = DateTime.UtcNow,
                ThemeDesign = Theme.light,
                Wins = 0,
                Defeats = 0,
                CountMasterTime = 0
            };

            await _repository.AddUserAsync(user, ct);
            try
            {
                await _repository.SaveChangesAsync(ct);
            }
            catch (DbUpdateException ex) when (IsUniqueViolation(ex))
            {
                // Race condition: another request created the same login between IsLoginTakenAsync and SaveChanges.
                throw new ConflictException("USERNAME_TAKEN", "Username is already taken");
            }

            return new RegisterResponse { UserId = user.UserId, Username = user.Login };
        }

        private static bool IsUniqueViolation(DbUpdateException ex)
            => ex.InnerException is PostgresException pg && pg.SqlState == PostgresErrorCodes.UniqueViolation;

        public async Task<LoginResponse> LoginAsync(LoginRequest request, CancellationToken ct = default)
        {
            var result = await _loginValidator.ValidateAsync(request, ct);
            if (!result.IsValid)
                throw new ValidationException("Invalid login data");

            var user = await _repository.GetByLoginAsync(request.Username, ct);
            if (user is null || !PasswordHasher.VerifyPassword(request.Password, user.PasswordHash))
                throw new UnauthorizedException("Invalid credentials");

            var token = _tokenService.Generate(user.UserId, user.Login);

            return new LoginResponse
            {
                AccessToken = token.AccessToken,
                TokenType = token.TokenType,
                ExpiresInSeconds = token.ExpiresInSeconds,
                Stats = new UserStatsDto
                {
                    Wins = user.Wins,
                    Defeats = user.Defeats,
                    MasterCount = user.CountMasterTime
                }
            };
        }

        public Task LogoutAsync(string token, CancellationToken ct = default)
        {
            if (string.IsNullOrWhiteSpace(token))
                throw new UnauthorizedException("Token is missing");

            try
            {
                var handler = new JwtSecurityTokenHandler();
                var jwt = handler.ReadJwtToken(token);
                var jti = jwt.Id;
                if (!string.IsNullOrEmpty(jti))
                {
                    RevokedTokens[jti] = jwt.ValidTo;
                    PruneExpired();
                }
            }
            catch
            {
                throw new UnauthorizedException("Invalid token");
            }

            return Task.CompletedTask;
        }

        public bool IsTokenRevoked(string jti)
            => RevokedTokens.ContainsKey(jti);

        public async Task<ProfileResponse> GetProfileAsync(Guid userId, CancellationToken ct = default)
        {
            var user = await _repository.GetByIdAsync(userId, ct)
                ?? throw new NotFoundException("User not found");
            return ToProfile(user);
        }

        public async Task<ProfileResponse> UpdateThemeAsync(Guid userId, UpdateThemeRequest request, CancellationToken ct = default)
        {
            if (!Enum.TryParse<Theme>(request.Theme, ignoreCase: true, out var theme))
                throw new ValidationException("theme", "Theme must be 'light' or 'dark'");

            var user = await _repository.GetByIdAsync(userId, ct)
                ?? throw new NotFoundException("User not found");

            user.ThemeDesign = theme;
            await _repository.SaveChangesAsync(ct);
            return ToProfile(user);
        }

        private static ProfileResponse ToProfile(User user) => new()
        {
            UserId = user.UserId,
            Username = user.Login,
            RegistrationDate = user.RegistrationDate,
            Theme = user.ThemeDesign.ToString(),
            Stats = new UserStatsDto
            {
                Wins = user.Wins,
                Defeats = user.Defeats,
                MasterCount = user.CountMasterTime
            }
        };

        private static void PruneExpired()
        {
            var now = DateTime.UtcNow;
            foreach (var kv in RevokedTokens)
            {
                if (kv.Value < now)
                    RevokedTokens.TryRemove(kv.Key, out _);
            }
        }
    }
}
