using Identity.Application.DTOs;

namespace Identity.Application.Interfaces
{
    public interface IAuthService
    {
        Task<string?> RegisterAsync(RegisterUserDto dto);

        Task<string?> LoginAsync(LoginUserDto dto);
    }
}