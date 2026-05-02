namespace Shared.Auth
{
    public interface ICurrentUser
    {
        Guid? UserId { get; }
        string? Username { get; }
        bool IsAuthenticated { get; }
        Guid RequireUserId();
    }
}
