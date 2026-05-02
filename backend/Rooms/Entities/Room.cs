using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace Rooms.Entities
{
    public enum AccessMode
    {
        PUBLIC,
        PRIVATE
    }

    public enum RoomStatus
    {
        ACTIVE,
        PAUSED,
        FINISHED
    }

    [Table("rooms")]
    public class Room
    {
        [Key]
        [Column("room_id")]
        public Guid RoomId { get; set; }

        [Required]
        [MaxLength(8)]
        [Column("room_code")]
        public string RoomCode { get; set; } = string.Empty;

        [Required]
        [MaxLength(100)]
        [Column("name")]
        public string Name { get; set; } = string.Empty;

        [Required]
        [Column("master_id")]
        public Guid MasterId { get; set; }

        [Column("access_mode")]
        public AccessMode AccessMode { get; set; }

        [Column("status")]
        public RoomStatus Status { get; set; } = RoomStatus.ACTIVE;

        [Column("created_at")]
        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

        [Column("finished_at")]
        public DateTime? FinishedAt { get; set; }

        public List<RoomParticipant> Participants { get; set; } = new();
    }
}
