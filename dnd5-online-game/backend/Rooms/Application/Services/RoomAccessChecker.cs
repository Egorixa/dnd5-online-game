using Microsoft.EntityFrameworkCore;
using Rooms.Application.Interfaces;
using Rooms.Data;
using Rooms.Entities;
using Shared.Errors;

namespace Rooms.Application.Services
{
    public class RoomAccessChecker : IRoomAccessChecker
    {
        private readonly RoomsDbContext _context;

        public RoomAccessChecker(RoomsDbContext context)
        {
            _context = context;
        }

        public async Task<RoomAccessInfo> RequireParticipantAsync(Guid userId, Guid roomId, CancellationToken ct = default)
        {
            var room = await _context.Rooms
                .Include(r => r.Participants)
                .FirstOrDefaultAsync(r => r.RoomId == roomId, ct)
                ?? throw new NotFoundException("Room not found");

            var participant = room.Participants
                .FirstOrDefault(p => p.UserId == userId && p.LeftAt == null)
                ?? throw new ForbiddenException("User is not a participant of this room");

            return new RoomAccessInfo(room, participant, room.MasterId == userId);
        }

        public async Task<RoomAccessInfo> RequireMasterAsync(Guid userId, Guid roomId, CancellationToken ct = default)
        {
            var info = await RequireParticipantAsync(userId, roomId, ct);
            if (!info.IsMaster)
                throw new ForbiddenException("Only the master of the room can perform this action");
            return info;
        }
    }
}
