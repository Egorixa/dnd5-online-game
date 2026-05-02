namespace Shared.Auth
{
    public interface IJwtTokenService
    {
        JwtTokenResult Generate(Guid userId, string username);
    }

    public record JwtTokenResult(string AccessToken, string TokenType, int ExpiresInSeconds);
}
