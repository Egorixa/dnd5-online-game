using Microsoft.AspNetCore.SignalR;
using RealTime.Connections;
using RealTime.Hubs;
using Shared.RealTime;

namespace RealTime.Notifications
{
    public class SignalRRoomNotifier : IRoomNotifier
    {
        private readonly IHubContext<RoomHub> _hub;
        private readonly IConnectionTracker _tracker;

        public SignalRRoomNotifier(IHubContext<RoomHub> hub, IConnectionTracker tracker)
        {
            _hub = hub;
            _tracker = tracker;
        }

        public Task NotifyAsync(Guid roomId, string eventName, object payload, CancellationToken ct = default)
            => _hub.Clients.Group(RoomHub.RoomGroup(roomId)).SendAsync(eventName, payload, ct);

        public Task NotifyUserAsync(Guid userId, string eventName, object payload, CancellationToken ct = default)
            => _hub.Clients.Group(RoomHub.UserGroup(userId)).SendAsync(eventName, payload, ct);

        public async Task RemoveUserFromRoomGroupAsync(Guid userId, Guid roomId, CancellationToken ct = default)
        {
            var groupName = RoomHub.RoomGroup(roomId);
            foreach (var connectionId in _tracker.GetConnections(userId))
                await _hub.Groups.RemoveFromGroupAsync(connectionId, groupName, ct);
        }
    }
}
