namespace Identity.Application.DTOs
{
    public class RegisterRequest
    {
        public string Username { get; set; } = string.Empty;
        public string Password { get; set; } = string.Empty;
    }

    public class RegisterResponse
    {
        public Guid UserId { get; set; }
        public string Username { get; set; } = string.Empty;
    }

    public class LoginRequest
    {
        public string Username { get; set; } = string.Empty;
        public string Password { get; set; } = string.Empty;
    }

    public class LoginResponse
    {
        public string AccessToken { get; set; } = string.Empty;
        public string TokenType { get; set; } = "Bearer";
        public int ExpiresInSeconds { get; set; }
        public UserStatsDto Stats { get; set; } = new();
    }

    public class UserStatsDto
    {
        public int Wins { get; set; }
        public int Defeats { get; set; }
        public int MasterCount { get; set; }
    }

    public class ProfileResponse
    {
        public Guid UserId { get; set; }
        public string Username { get; set; } = string.Empty;
        public DateTime RegistrationDate { get; set; }
        public string Theme { get; set; } = "light";
        public UserStatsDto Stats { get; set; } = new();
    }

    public class UpdateThemeRequest
    {
        public string Theme { get; set; } = string.Empty;
    }
}
