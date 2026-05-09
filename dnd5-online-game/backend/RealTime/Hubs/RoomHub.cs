using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.SignalR;
using RealTime.Connections;
using Rooms.Application.Interfaces;
using Shared.Auth;
using Shared.Errors;

namespace RealTime.Hubs
{
    [Authorize]
    public class RoomHub : Hub
    {
        private readonly IRoomAccessChecker _access;
        private readonly ICurrentUser _currentUser;
        private readonly IConnectionTracker _tracker;

        public RoomHub(IRoomAccessChecker access, ICurrentUser currentUser, IConnectionTracker tracker)
        {
            _access = access;
            _currentUser = currentUser;
            _tracker = tracker;
        }

        public override async Task OnConnectedAsync()
        {
            var userId = _currentUser.UserId;
            if (userId.HasValue)
            {
                _tracker.Track(userId.Value, Context.ConnectionId);
                await Groups.AddToGroupAsync(Context.ConnectionId, UserGroup(userId.Value));
            }
            await base.OnConnectedAsync();
        }

        public override async Task OnDisconnectedAsync(Exception? exception)
        {
            var userId = _currentUser.UserId;
            if (userId.HasValue)
                _tracker.Untrack(userId.Value, Context.ConnectionId);
            await base.OnDisconnectedAsync(exception);
        }

        public async Task JoinRoom(Guid roomId)
        {
            var userId = _currentUser.UserId
                ?? throw new UnauthorizedException("Hub connection has no user");
            await _access.RequireParticipantAsync(userId, roomId, Context.ConnectionAborted);
            await Groups.AddToGroupAsync(Context.ConnectionId, RoomGroup(roomId));
        }

        public Task LeaveRoom(Guid roomId)
            => Groups.RemoveFromGroupAsync(Context.ConnectionId, RoomGroup(roomId));

        public static string RoomGroup(Guid roomId) => $"room:{roomId}";
        public static string UserGroup(Guid userId) => $"user:{userId}";
    }
}
