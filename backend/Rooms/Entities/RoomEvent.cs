using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace Rooms.Entities
{
    public enum RoomEventType
    {
        ParticipantJoined,
        ParticipantLeft,
        ParticipantKicked,
        CharacterCreated,
        CharacterUpdated,
        CharacterDeleted,
        DiceRolled,
        RoomFinished,
        RoomPaused
    }

    [Table("room_events")]
    public class RoomEvent
    {
        [Key]
        [Column("event_id")]
        public Guid EventId { get; set; }

        [Required]
        [Column("room_id")]
        public Guid RoomId { get; set; }

        [Column("type")]
        public RoomEventType Type { get; set; }

        [Column("actor_user_id")]
        public Guid? ActorUserId { get; set; }

        [Column("payload", TypeName = "jsonb")]
        public string Payload { get; set; } = "{}";

        [Column("created_at")]
        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    }
}
