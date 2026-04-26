using Shared.Lookups;

namespace Characters.Application.DTOs
{
    public class CharacterUpsertRequest
    {
        // Basic
        public string? Name { get; set; }
        public string? PlayerName { get; set; }
        public Race? Race { get; set; }
        public CharacterClass? Class { get; set; }
        public int? Level { get; set; }
        public Background? Background { get; set; }
        public Alignment? Alignment { get; set; }
        public int? ExperiencePoints { get; set; }

        // Abilities
        public int? Strength { get; set; }
        public int? Dexterity { get; set; }
        public int? Constitution { get; set; }
        public int? Intelligence { get; set; }
        public int? Wisdom { get; set; }
        public int? Charisma { get; set; }

        // Combat
        public int? ArmorClass { get; set; }
        public int? InitiativeBonus { get; set; }
        public int? Speed { get; set; }
        public int? MaxHp { get; set; }
        public int? CurrentHp { get; set; }
        public int? TempHp { get; set; }
        public HitDie? HitDieType { get; set; }
        public int? HitDiceRemaining { get; set; }
        public int? DeathSaveSuccesses { get; set; }
        public int? DeathSaveFailures { get; set; }
        public bool? Inspiration { get; set; }

        // Currency
        public int? CopperPieces { get; set; }
        public int? SilverPieces { get; set; }
        public int? ElectrumPieces { get; set; }
        public int? GoldPieces { get; set; }
        public int? PlatinumPieces { get; set; }

        // Free-form
        public string? Equipment { get; set; }
        public string? OtherProficiencies { get; set; }
        public string? CharacterTraits { get; set; }
        public string? Ideals { get; set; }
        public string? Bonds { get; set; }
        public string? Flaws { get; set; }
        public string? FeaturesAndTraits { get; set; }

        // Appearance & background
        public string? Eyes { get; set; }
        public int? Age { get; set; }
        public int? Height { get; set; }
        public int? Weight { get; set; }
        public string? Skin { get; set; }
        public string? Hair { get; set; }
        public string? AlliesAndOrganizations { get; set; }
        public string? Backstory { get; set; }
        public string? Treasure { get; set; }
        public string? AdditionalNotes { get; set; }
        public string? DistinguishingMarks { get; set; }

        // Skill / save proficiencies (full replace if provided)
        public Dictionary<Skill, ProficiencyLevel>? SkillProficiencies { get; set; }
        public Dictionary<Ability, ProficiencyLevel>? SaveProficiencies { get; set; }

        // Attacks / spells (full replace if provided)
        public List<AttackDto>? Attacks { get; set; }

        // Spellcasting
        public CharacterClass? SpellcastingClass { get; set; }
        public int? SpellSlotsTotal { get; set; }
        public int? SpellSlotsUsed { get; set; }
        public int? PreparedLimit { get; set; }
        public List<SpellDto>? Spells { get; set; }

        // Optimistic concurrency
        public uint? RowVersion { get; set; }
    }

    public class AttackDto
    {
        public Guid? AttackId { get; set; }
        public string Name { get; set; } = string.Empty;
        public int AttackBonus { get; set; }
        public string Damage { get; set; } = string.Empty;
    }

    public class SpellDto
    {
        public Guid? SpellId { get; set; }
        public string Name { get; set; } = string.Empty;
        public int Level { get; set; }
        public string School { get; set; } = string.Empty;
        public string CastingTime { get; set; } = string.Empty;
        public string Range { get; set; } = string.Empty;
        public string Components { get; set; } = string.Empty;
        public string Duration { get; set; } = string.Empty;
        public string Description { get; set; } = string.Empty;
        public bool Prepared { get; set; }
    }

    public class CharacterResponse
    {
        public Guid CharacterId { get; set; }
        public Guid RoomId { get; set; }
        public Guid OwnerUserId { get; set; }
        public uint RowVersion { get; set; }

        // Basic
        public string Name { get; set; } = string.Empty;
        public string PlayerName { get; set; } = string.Empty;
        public Race? Race { get; set; }
        public CharacterClass? Class { get; set; }
        public int Level { get; set; }
        public Background? Background { get; set; }
        public Alignment? Alignment { get; set; }
        public int ExperiencePoints { get; set; }

        // Abilities + computed modifiers
        public int Strength { get; set; }
        public int Dexterity { get; set; }
        public int Constitution { get; set; }
        public int Intelligence { get; set; }
        public int Wisdom { get; set; }
        public int Charisma { get; set; }

        public AbilityModifiers Modifiers { get; set; } = new();

        // Computed values
        public int ProficiencyBonus { get; set; }
        public int PassivePerception { get; set; }
        public int? SpellSaveDc { get; set; }
        public int? SpellAttackBonus { get; set; }
        public int HitDiceTotal { get; set; }

        // Combat
        public int ArmorClass { get; set; }
        public int InitiativeBonus { get; set; }
        public int Speed { get; set; }
        public int MaxHp { get; set; }
        public int CurrentHp { get; set; }
        public int TempHp { get; set; }
        public HitDie? HitDieType { get; set; }
        public int HitDiceRemaining { get; set; }
        public int DeathSaveSuccesses { get; set; }
        public int DeathSaveFailures { get; set; }
        public bool Inspiration { get; set; }

        // Currency
        public int CopperPieces { get; set; }
        public int SilverPieces { get; set; }
        public int ElectrumPieces { get; set; }
        public int GoldPieces { get; set; }
        public int PlatinumPieces { get; set; }

        // Free-form
        public string Equipment { get; set; } = string.Empty;
        public string OtherProficiencies { get; set; } = string.Empty;
        public string CharacterTraits { get; set; } = string.Empty;
        public string Ideals { get; set; } = string.Empty;
        public string Bonds { get; set; } = string.Empty;
        public string Flaws { get; set; } = string.Empty;
        public string FeaturesAndTraits { get; set; } = string.Empty;

        public string Eyes { get; set; } = string.Empty;
        public int Age { get; set; }
        public int Height { get; set; }
        public int Weight { get; set; }
        public string Skin { get; set; } = string.Empty;
        public string Hair { get; set; } = string.Empty;
        public string AlliesAndOrganizations { get; set; } = string.Empty;
        public string Backstory { get; set; } = string.Empty;
        public string Treasure { get; set; } = string.Empty;
        public string AdditionalNotes { get; set; } = string.Empty;
        public string DistinguishingMarks { get; set; } = string.Empty;

        public Dictionary<Skill, SkillView> Skills { get; set; } = new();
        public Dictionary<Ability, SaveView> Saves { get; set; } = new();
        public List<AttackDto> Attacks { get; set; } = new();

        public CharacterClass? SpellcastingClass { get; set; }
        public int SpellSlotsTotal { get; set; }
        public int SpellSlotsUsed { get; set; }
        public int PreparedLimit { get; set; }
        public List<SpellResponseDto> Spells { get; set; } = new();
    }

    public class AbilityModifiers
    {
        public int Strength { get; set; }
        public int Dexterity { get; set; }
        public int Constitution { get; set; }
        public int Intelligence { get; set; }
        public int Wisdom { get; set; }
        public int Charisma { get; set; }
    }

    public class SkillView
    {
        public ProficiencyLevel Level { get; set; }
        public int Bonus { get; set; }
    }

    public class SaveView
    {
        public ProficiencyLevel Level { get; set; }
        public int Bonus { get; set; }
    }

    public class SpellResponseDto : SpellDto
    {
        public new Guid SpellId { get; set; }
    }
}
