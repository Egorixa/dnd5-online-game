namespace Identity.Application.Interfaces
{
    public interface IUserLookupService
    {
        Task<string?> GetUsernameAsync(Guid userId, CancellationToken ct = default);
        Task<Dictionary<Guid, string>> GetUsernamesAsync(IEnumerable<Guid> userIds, CancellationToken ct = default);
    }
}
