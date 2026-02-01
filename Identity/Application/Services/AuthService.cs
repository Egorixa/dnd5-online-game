using Identity.Application.DTOs;
using Identity.Application.Helpers;
using Identity.Application.Interfaces;
using Identity.Entities;

namespace Identity.Application.Services
{
    public class AuthService : IAuthService
    {
        private readonly IAuthRepository _repository;

        public AuthService(IAuthRepository repository)
        {
            _repository = repository;
        }

        public async Task<string?> RegisterAsync(RegisterUserDto dto)
        {
            if (dto.Password != dto.ConfirmPassword)
                return "Пароли не совпадают.";

            if (await _repository.IsLoginTakenAsync(dto.Login))
                return "Пользователь с таким логином уже существует.";

            var user = new User
            {
                UserId = Guid.NewGuid(),
                Login = dto.Login,
                PasswordHash = PasswordHasher.HashPassword(dto.Password),
                RegistrationDate = DateTime.UtcNow,
                ThemeDesign = Theme.light 
            };

            await _repository.AddUserAsync(user);
            await _repository.SaveChangesAsync();

            return null; 
        }

        public async Task<string?> LoginAsync(LoginUserDto dto)
        {
            var user = await _repository.GetByLoginAsync(dto.Login);

            if (user == null)
                return null; 

            bool isPasswordValid = PasswordHasher.VerifyPassword(dto.Password, user.PasswordHash);

            if (!isPasswordValid)
                return null; 

            return $"FAKE-JWT-TOKEN-FOR-{user.UserId}";
        }
    }
}