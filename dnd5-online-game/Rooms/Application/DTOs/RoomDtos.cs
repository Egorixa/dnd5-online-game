using Rooms.Entities;

namespace Rooms.Application.DTOs
{
    public class CreateRoomRequest
    {
        public AccessMode AccessMode { get; set; } = AccessMode.PRIVATE;
    }

    public class CreateRoomResponse
    {
        public Guid RoomId { get; set; }
        public string RoomCode { get; set; } = string.Empty;
        public AccessMode AccessMode { get; set; }
        public DateTime CreatedAt { get; set; }
    }

    public class PublicRoomDto
    {
        public Guid RoomId { get; set; }
        public string RoomCode { get; set; } = string.Empty;
        public Guid MasterId { get; set; }
        public int PlayersCount { get; set; }
        public DateTime CreatedAt { get; set; }
    }

    public class JoinRoomResponse
    {
        public Guid RoomId { get; set; }
        public string RoomCode { get; set; } = string.Empty;
        public AccessMode AccessMode { get; set; }
        public Guid ParticipantId { get; set; }
        public ParticipantRole Role { get; set; }
        public RoomStateDto CurrentState { get; set; } = new();
    }

    public class RoomStateDto
    {
        public Guid RoomId { get; set; }
        public RoomStatus Status { get; set; }
        public Guid MasterId { get; set; }
        public List<RoomParticipantDto> Participants { get; set; } = new();
    }

    public class RoomParticipantDto
    {
        public Guid ParticipantId { get; set; }
        public Guid UserId { get; set; }
        public ParticipantRole Role { get; set; }
        public DateTime JoinedAt { get; set; }
    }

    public class KickParticipantRequest
    {
        public Guid? TargetUserId { get; set; }
        public Guid? TargetParticipantId { get; set; }
    }

    public class FinishRoomRequest
    {
        public List<Guid> Winners { get; set; } = new();
        public List<Guid> Losers { get; set; } = new();
    }

    public class RoomEventDto
    {
        public Guid EventId { get; set; }
        public RoomEventType Type { get; set; }
        public Guid? ActorUserId { get; set; }
        public string Payload { get; set; } = "{}";
        public DateTime CreatedAt { get; set; }
    }
}
