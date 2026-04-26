namespace Identity.Application.Interfaces
{
    public interface IUserStatsService
    {
        Task IncrementWinsAsync(IEnumerable<Guid> userIds, CancellationToken ct = default);
        Task IncrementDefeatsAsync(IEnumerable<Guid> userIds, CancellationToken ct = default);
        Task IncrementMasterCountAsync(Guid userId, CancellationToken ct = default);
    }
}
