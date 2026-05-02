using Microsoft.AspNetCore.SignalR;
using RealTime.Hubs;
using Shared.RealTime;

namespace RealTime.Notifications
{
    public class SignalRRoomNotifier : IRoomNotifier
    {
        private readonly IHubContext<RoomHub> _hub;

        public SignalRRoomNotifier(IHubContext<RoomHub> hub)
        {
            _hub = hub;
        }

        public Task NotifyAsync(Guid roomId, string eventName, object payload, CancellationToken ct = default)
            => _hub.Clients.Group(RoomHub.RoomGroup(roomId)).SendAsync(eventName, payload, ct);

        public Task NotifyUserAsync(Guid userId, string eventName, object payload, CancellationToken ct = default)
            => _hub.Clients.Group(RoomHub.UserGroup(userId)).SendAsync(eventName, payload, ct);
    }
}
