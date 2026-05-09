using Rooms.Entities;

namespace Rooms.Application.Interfaces
{
    public interface IRoomAccessChecker
    {
        Task<RoomAccessInfo> RequireParticipantAsync(Guid userId, Guid roomId, CancellationToken ct = default);
        Task<RoomAccessInfo> RequireMasterAsync(Guid userId, Guid roomId, CancellationToken ct = default);
    }

    public record RoomAccessInfo(Room Room, RoomParticipant Participant, bool IsMaster);
}
