using Identity.Application.DTOs;
using Identity.Application.Interfaces;
using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Shared.Auth;

namespace Identity.Controllers
{
    [ApiController]
    [Route("auth")]
    public class AuthController : ControllerBase
    {
        private readonly IAuthService _authService;
        private readonly ICurrentUser _currentUser;

        public AuthController(IAuthService authService, ICurrentUser currentUser)
        {
            _authService = authService;
            _currentUser = currentUser;
        }

        [HttpPost("register")]
        [ProducesResponseType(typeof(RegisterResponse), StatusCodes.Status201Created)]
        public async Task<IActionResult> Register([FromBody] RegisterRequest request, CancellationToken ct)
        {
            var response = await _authService.RegisterAsync(request, ct);
            return StatusCode(StatusCodes.Status201Created, response);
        }

        [HttpPost("login")]
        [ProducesResponseType(typeof(LoginResponse), StatusCodes.Status200OK)]
        public async Task<IActionResult> Login([FromBody] LoginRequest request, CancellationToken ct)
        {
            var response = await _authService.LoginAsync(request, ct);
            return Ok(response);
        }

        [Authorize(AuthenticationSchemes = JwtBearerDefaults.AuthenticationScheme)]
        [HttpPost("logout")]
        public async Task<IActionResult> Logout(CancellationToken ct)
        {
            var token = await HttpContext.GetTokenAsync(JwtBearerDefaults.AuthenticationScheme, "access_token");
            if (string.IsNullOrEmpty(token))
            {
                var auth = HttpContext.Request.Headers["Authorization"].ToString();
                if (auth.StartsWith("Bearer ", StringComparison.OrdinalIgnoreCase))
                    token = auth.Substring("Bearer ".Length).Trim();
            }
            await _authService.LogoutAsync(token!, ct);
            return NoContent();
        }

        [Authorize(AuthenticationSchemes = JwtBearerDefaults.AuthenticationScheme)]
        [HttpGet("profile")]
        [ProducesResponseType(typeof(ProfileResponse), StatusCodes.Status200OK)]
        public async Task<IActionResult> GetProfile(CancellationToken ct)
        {
            var userId = _currentUser.RequireUserId();
            var profile = await _authService.GetProfileAsync(userId, ct);
            return Ok(profile);
        }

        [Authorize(AuthenticationSchemes = JwtBearerDefaults.AuthenticationScheme)]
        [HttpPut("theme")]
        [ProducesResponseType(typeof(ProfileResponse), StatusCodes.Status200OK)]
        public async Task<IActionResult> UpdateTheme([FromBody] UpdateThemeRequest request, CancellationToken ct)
        {
            var userId = _currentUser.RequireUserId();
            var profile = await _authService.UpdateThemeAsync(userId, request, ct);
            return Ok(profile);
        }
    }
}
