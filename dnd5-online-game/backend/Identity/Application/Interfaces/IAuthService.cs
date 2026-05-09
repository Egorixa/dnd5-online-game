using Identity.Application.DTOs;

namespace Identity.Application.Interfaces
{
    public interface IAuthService
    {
        Task<RegisterResponse> RegisterAsync(RegisterRequest request, CancellationToken ct = default);
        Task<LoginResponse> LoginAsync(LoginRequest request, CancellationToken ct = default);
        Task LogoutAsync(string token, CancellationToken ct = default);
        bool IsTokenRevoked(string jti);

        Task<ProfileResponse> GetProfileAsync(Guid userId, CancellationToken ct = default);
        Task<ProfileResponse> UpdateThemeAsync(Guid userId, UpdateThemeRequest request, CancellationToken ct = default);
    }
}
