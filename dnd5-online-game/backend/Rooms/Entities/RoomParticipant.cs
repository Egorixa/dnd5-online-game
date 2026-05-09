using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace Rooms.Entities
{
    public enum ParticipantRole
    {
        MASTER,
        PLAYER
    }

    [Table("room_participants")]
    public class RoomParticipant
    {
        [Key]
        [Column("participant_id")]
        public Guid ParticipantId { get; set; }

        [Required]
        [Column("room_id")]
        public Guid RoomId { get; set; }

        [Required]
        [Column("user_id")]
        public Guid UserId { get; set; }

        [Column("role")]
        public ParticipantRole Role { get; set; }

        [Column("joined_at")]
        public DateTime JoinedAt { get; set; } = DateTime.UtcNow;

        [Column("left_at")]
        public DateTime? LeftAt { get; set; }

        public Room? Room { get; set; }
    }
}
