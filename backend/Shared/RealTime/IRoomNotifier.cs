namespace Shared.RealTime
{
    public interface IRoomNotifier
    {
        Task NotifyAsync(Guid roomId, string eventName, object payload, CancellationToken ct = default);
        Task NotifyUserAsync(Guid userId, string eventName, object payload, CancellationToken ct = default);
    }
}
