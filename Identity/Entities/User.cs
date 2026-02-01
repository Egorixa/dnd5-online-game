using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace Identity.Entities
{
    public enum Theme
    {
        light,
        dark
    }

    [Table("users")] 
    public class User
    {
        [Key] 
        [Column("user_id")] 
        public Guid UserId { get; set; }

        [Required]
        [MaxLength(20)] 
        [Column("login")]
        public string Login { get; set; } = string.Empty;

        [Required]
        [Column("password_hash")] // ХЭШ!
        public string PasswordHash { get; set; } = string.Empty;

        [Column("theme_design")]
        public Theme ThemeDesign { get; set; } = Theme.light; 

        [Column("wins")]
        public int Wins { get; set; } = 0;

        [Column("defeats")]
        public int Defeats { get; set; } = 0;

        [Column("count_master_time")]
        public int CountMasterTime { get; set; } = 0;

        [Column("registration_date")]
        public DateTime RegistrationDate { get; set; } = DateTime.UtcNow; 
    }
}