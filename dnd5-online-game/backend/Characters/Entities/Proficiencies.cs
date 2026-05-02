using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Shared.Lookups;

namespace Characters.Entities
{
    [Table("character_skill_proficiencies")]
    public class SkillProficiency
    {
        [Column("character_id")] public Guid CharacterId { get; set; }
        [Column("skill")] public Skill Skill { get; set; }
        [Column("level")] public ProficiencyLevel Level { get; set; }
    }

    [Table("character_save_proficiencies")]
    public class SaveProficiency
    {
        [Column("character_id")] public Guid CharacterId { get; set; }
        [Column("ability")] public Ability Ability { get; set; }
        [Column("level")] public ProficiencyLevel Level { get; set; }
    }

    [Table("character_attacks")]
    public class Attack
    {
        [Key] [Column("attack_id")] public Guid AttackId { get; set; }
        [Column("character_id")] public Guid CharacterId { get; set; }
        [Required] [MaxLength(50)] [Column("name")] public string Name { get; set; } = string.Empty;
        [Column("attack_bonus")] public int AttackBonus { get; set; }
        [Required] [MaxLength(50)] [Column("damage")] public string Damage { get; set; } = string.Empty;
    }

    [Table("character_spells")]
    public class Spell
    {
        [Key] [Column("spell_id")] public Guid SpellId { get; set; }
        [Column("character_id")] public Guid CharacterId { get; set; }
        [Required] [MaxLength(60)] [Column("name")] public string Name { get; set; } = string.Empty;
        [Column("level")] public int Level { get; set; }
        [MaxLength(40)] [Column("school")] public string School { get; set; } = string.Empty;
        [MaxLength(60)] [Column("casting_time")] public string CastingTime { get; set; } = string.Empty;
        [MaxLength(60)] [Column("range")] public string Range { get; set; } = string.Empty;
        [MaxLength(60)] [Column("components")] public string Components { get; set; } = string.Empty;
        [MaxLength(60)] [Column("duration")] public string Duration { get; set; } = string.Empty;
        [MaxLength(2000)] [Column("description")] public string Description { get; set; } = string.Empty;
        [Column("prepared")] public bool Prepared { get; set; }
    }
}
