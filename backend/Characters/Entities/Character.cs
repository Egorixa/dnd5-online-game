using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Shared.Lookups;

namespace Characters.Entities
{
    [Table("characters")]
    public class Character
    {
        [Key]
        [Column("character_id")]
        public Guid CharacterId { get; set; }

        // Null when this row is a template stored in the user's account
        // (per command TZ 4.1.1.3.2.4 / private TZ 6.4 п.6).
        [Column("room_id")]
        public Guid? RoomId { get; set; }

        [Required]
        [Column("owner_user_id")]
        public Guid OwnerUserId { get; set; }

        [Column("is_archived")]
        public bool IsArchived { get; set; }

        [Column("row_version")]
        [ConcurrencyCheck]
        public uint RowVersion { get; set; }

        // Basic
        [MaxLength(50)] [Column("name")] public string Name { get; set; } = string.Empty;
        [MaxLength(50)] [Column("player_name")] public string PlayerName { get; set; } = string.Empty;
        [Column("race")] public Race? Race { get; set; }
        [Column("class")] public CharacterClass? Class { get; set; }
        [Column("level")] public int Level { get; set; } = 1;
        [Column("background")] public Background? Background { get; set; }
        [Column("alignment")] public Alignment? Alignment { get; set; }
        [Column("experience_points")] public int ExperiencePoints { get; set; }

        // Abilities
        [Column("strength")] public int Strength { get; set; } = 10;
        [Column("dexterity")] public int Dexterity { get; set; } = 10;
        [Column("constitution")] public int Constitution { get; set; } = 10;
        [Column("intelligence")] public int Intelligence { get; set; } = 10;
        [Column("wisdom")] public int Wisdom { get; set; } = 10;
        [Column("charisma")] public int Charisma { get; set; } = 10;

        // Combat
        [Column("armor_class")] public int ArmorClass { get; set; } = 10;
        [Column("initiative_bonus")] public int InitiativeBonus { get; set; }
        [Column("speed")] public int Speed { get; set; } = 30;
        [Column("max_hp")] public int MaxHp { get; set; } = 1;
        [Column("current_hp")] public int CurrentHp { get; set; } = 1;
        [Column("temp_hp")] public int TempHp { get; set; }
        [Column("hit_die_type")] public HitDie? HitDieType { get; set; }
        [Column("hit_dice_remaining")] public int HitDiceRemaining { get; set; }
        [Column("death_save_successes")] public int DeathSaveSuccesses { get; set; }
        [Column("death_save_failures")] public int DeathSaveFailures { get; set; }
        [Column("inspiration")] public bool Inspiration { get; set; }

        // Currency
        [Column("copper_pieces")] public int CopperPieces { get; set; }
        [Column("silver_pieces")] public int SilverPieces { get; set; }
        [Column("electrum_pieces")] public int ElectrumPieces { get; set; }
        [Column("gold_pieces")] public int GoldPieces { get; set; }
        [Column("platinum_pieces")] public int PlatinumPieces { get; set; }

        // Free-form text
        [MaxLength(2000)] [Column("equipment")] public string Equipment { get; set; } = string.Empty;
        [MaxLength(1000)] [Column("other_proficiencies")] public string OtherProficiencies { get; set; } = string.Empty;
        [MaxLength(1500)] [Column("character_traits")] public string CharacterTraits { get; set; } = string.Empty;
        [MaxLength(1500)] [Column("ideals")] public string Ideals { get; set; } = string.Empty;
        [MaxLength(1500)] [Column("bonds")] public string Bonds { get; set; } = string.Empty;
        [MaxLength(1500)] [Column("flaws")] public string Flaws { get; set; } = string.Empty;
        [MaxLength(3000)] [Column("features_and_traits")] public string FeaturesAndTraits { get; set; } = string.Empty;

        // Appearance & background
        [MaxLength(30)] [Column("eyes")] public string Eyes { get; set; } = string.Empty;
        [Column("age")] public int Age { get; set; }
        [Column("height")] public int Height { get; set; }
        [Column("weight")] public int Weight { get; set; }
        [MaxLength(30)] [Column("skin")] public string Skin { get; set; } = string.Empty;
        [MaxLength(30)] [Column("hair")] public string Hair { get; set; } = string.Empty;
        [MaxLength(1500)] [Column("allies_and_organizations")] public string AlliesAndOrganizations { get; set; } = string.Empty;
        [MaxLength(3000)] [Column("backstory")] public string Backstory { get; set; } = string.Empty;
        [MaxLength(1500)] [Column("treasure")] public string Treasure { get; set; } = string.Empty;
        [MaxLength(1500)] [Column("additional_notes")] public string AdditionalNotes { get; set; } = string.Empty;
        [MaxLength(1500)] [Column("distinguishing_marks")] public string DistinguishingMarks { get; set; } = string.Empty;

        // Spellcasting
        [Column("spellcasting_class")] public CharacterClass? SpellcastingClass { get; set; }
        [Column("spell_slots_total")] public int SpellSlotsTotal { get; set; }
        [Column("spell_slots_used")] public int SpellSlotsUsed { get; set; }
        [Column("prepared_limit")] public int PreparedLimit { get; set; }

        [Column("created_at")] public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
        [Column("updated_at")] public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;

        public List<SkillProficiency> SkillProficiencies { get; set; } = new();
        public List<SaveProficiency> SaveProficiencies { get; set; } = new();
        public List<Attack> Attacks { get; set; } = new();
        public List<Spell> Spells { get; set; } = new();
    }
}
