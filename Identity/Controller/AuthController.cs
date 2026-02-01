using Identity.Application.DTOs;
using Identity.Application.Interfaces;
using Microsoft.AspNetCore.Mvc;

namespace Identity.Controllers
{
    [ApiController]
    [Route("api/auth")] 
    public class AuthController : ControllerBase
    {
        private readonly IAuthService _authService;

        public AuthController(IAuthService authService)
        {
            _authService = authService;
        }

        [HttpPost("register")]
        public async Task<IActionResult> Register([FromBody] RegisterUserDto dto)
        {
            var errorMessage = await _authService.RegisterAsync(dto);

            if (errorMessage != null)
            {
                return BadRequest(new { message = errorMessage });
            }

            return Ok(new { message = "Пользователь успешно зарегистрирован" });
        }

        [HttpPost("login")]
        public async Task<IActionResult> Login([FromBody] LoginUserDto dto)
        {
            var token = await _authService.LoginAsync(dto);

            if (token == null)
            {
                return Unauthorized(new { message = "Неверный логин или пароль" });
            }
            return Ok(new { token = token });
        }
    }
}