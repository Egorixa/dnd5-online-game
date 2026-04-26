using Rooms.Application.DTOs;

namespace Rooms.Application.Interfaces
{
    public interface IRoomService
    {
        Task<CreateRoomResponse> CreateAsync(Guid userId, CreateRoomRequest request, CancellationToken ct = default);
        Task<List<PublicRoomDto>> GetPublicAsync(int limit, int offset, CancellationToken ct = default);
        Task<JoinRoomResponse> JoinAsync(Guid userId, string roomCode, CancellationToken ct = default);
        Task LeaveAsync(Guid userId, Guid roomId, CancellationToken ct = default);
        Task KickAsync(Guid masterUserId, Guid roomId, KickParticipantRequest request, CancellationToken ct = default);
        Task FinishAsync(Guid masterUserId, Guid roomId, FinishRoomRequest request, CancellationToken ct = default);
        Task<List<RoomEventDto>> GetEventsAsync(Guid userId, Guid roomId, int limit, int offset, CancellationToken ct = default);
        Task<RoomStateDto> GetStateAsync(Guid userId, Guid roomId, CancellationToken ct = default);
    }
}
