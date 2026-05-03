package com.example.android.net.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

/** DTO для эндпоинтов /characters/* и /rooms/{id}/characters/* (Characters модуль). */
public class CharacterDtos {

    private CharacterDtos() {}

    // ----- enum-константы (передаются как строки благодаря JsonStringEnumConverter на бекенде) -----

    public static final class Race {
        public static final String DWARF = "Dwarf";
        public static final String ELF = "Elf";
        public static final String HALFLING = "Halfling";
        public static final String HUMAN = "Human";
        public static final String DRAGONBORN = "Dragonborn";
        public static final String GNOME = "Gnome";
        public static final String HALF_ELF = "HalfElf";
        public static final String HALF_ORC = "HalfOrc";
        public static final String TIEFLING = "Tiefling";
        private Race() {}
    }

    public static final class CharacterClass {
        public static final String BARBARIAN = "Barbarian";
        public static final String BARD = "Bard";
        public static final String CLERIC = "Cleric";
        public static final String DRUID = "Druid";
        public static final String FIGHTER = "Fighter";
        public static final String MONK = "Monk";
        public static final String PALADIN = "Paladin";
        public static final String RANGER = "Ranger";
        public static final String ROGUE = "Rogue";
        public static final String SORCERER = "Sorcerer";
        public static final String WARLOCK = "Warlock";
        public static final String WIZARD = "Wizard";
        private CharacterClass() {}
    }

    public static final class Background {
        public static final String ACOLYTE = "Acolyte";
        public static final String CHARLATAN = "Charlatan";
        public static final String CRIMINAL = "Criminal";
        public static final String ENTERTAINER = "Entertainer";
        public static final String FOLK_HERO = "FolkHero";
        public static final String GUILD_ARTISAN = "GuildArtisan";
        public static final String HERMIT = "Hermit";
        public static final String NOBLE = "Noble";
        public static final String OUTLANDER = "Outlander";
        public static final String SAGE = "Sage";
        public static final String SAILOR = "Sailor";
        public static final String SOLDIER = "Soldier";
        public static final String URCHIN = "Urchin";
        private Background() {}
    }

    public static final class Alignment {
        public static final String LAWFUL_GOOD = "LawfulGood";
        public static final String NEUTRAL_GOOD = "NeutralGood";
        public static final String CHAOTIC_GOOD = "ChaoticGood";
        public static final String LAWFUL_NEUTRAL = "LawfulNeutral";
        public static final String TRUE_NEUTRAL = "TrueNeutral";
        public static final String CHAOTIC_NEUTRAL = "ChaoticNeutral";
        public static final String LAWFUL_EVIL = "LawfulEvil";
        public static final String NEUTRAL_EVIL = "NeutralEvil";
        public static final String CHAOTIC_EVIL = "ChaoticEvil";
        private Alignment() {}
    }

    public static final class HitDie {
        public static final String D6 = "d6";
        public static final String D8 = "d8";
        public static final String D10 = "d10";
        public static final String D12 = "d12";
        private HitDie() {}
    }

    public static final class Ability {
        public static final String STRENGTH = "Strength";
        public static final String DEXTERITY = "Dexterity";
        public static final String CONSTITUTION = "Constitution";
        public static final String INTELLIGENCE = "Intelligence";
        public static final String WISDOM = "Wisdom";
        public static final String CHARISMA = "Charisma";
        private Ability() {}
    }

    public static final class Skill {
        public static final String ACROBATICS = "Acrobatics";
        public static final String ANIMAL_HANDLING = "AnimalHandling";
        public static final String ARCANA = "Arcana";
        public static final String ATHLETICS = "Athletics";
        public static final String DECEPTION = "Deception";
        public static final String HISTORY = "History";
        public static final String INSIGHT = "Insight";
        public static final String INTIMIDATION = "Intimidation";
        public static final String INVESTIGATION = "Investigation";
        public static final String MEDICINE = "Medicine";
        public static final String NATURE = "Nature";
        public static final String PERCEPTION = "Perception";
        public static final String PERFORMANCE = "Performance";
        public static final String PERSUASION = "Persuasion";
        public static final String RELIGION = "Religion";
        public static final String SLEIGHT_OF_HAND = "SleightOfHand";
        public static final String STEALTH = "Stealth";
        public static final String SURVIVAL = "Survival";
        private Skill() {}
    }

    public static final class ProficiencyLevel {
        public static final String NONE = "None";
        public static final String PROFICIENT = "Proficient";
        private ProficiencyLevel() {}
    }

    // ----- DTO -----

    public static class CharacterUpsertRequest {
        // Basic
        public String name;
        public String playerName;
        public String race;
        @SerializedName("class")
        public String characterClass;
        public Integer level;
        public String background;
        public String alignment;
        public Integer experiencePoints;

        // Abilities
        public Integer strength;
        public Integer dexterity;
        public Integer constitution;
        public Integer intelligence;
        public Integer wisdom;
        public Integer charisma;

        // Combat
        public Integer armorClass;
        public Integer initiativeBonus;
        public Integer speed;
        public Integer maxHp;
        public Integer currentHp;
        public Integer tempHp;
        public String hitDieType;
        public Integer hitDiceRemaining;
        public Integer deathSaveSuccesses;
        public Integer deathSaveFailures;
        public Boolean inspiration;

        // Currency
        public Integer copperPieces;
        public Integer silverPieces;
        public Integer electrumPieces;
        public Integer goldPieces;
        public Integer platinumPieces;

        // Free-form
        public String equipment;
        public String otherProficiencies;
        public String characterTraits;
        public String ideals;
        public String bonds;
        public String flaws;
        public String featuresAndTraits;

        // Appearance & background
        public String eyes;
        public Integer age;
        public Integer height;
        public Integer weight;
        public String skin;
        public String hair;
        public String alliesAndOrganizations;
        public String backstory;
        public String treasure;
        public String additionalNotes;
        public String distinguishingMarks;

        // Skill / save proficiencies
        public Map<String, String> skillProficiencies; // Skill -> ProficiencyLevel
        public Map<String, String> saveProficiencies;  // Ability -> ProficiencyLevel

        // Attacks / spells
        public List<AttackDto> attacks;

        // Spellcasting
        public String spellcastingClass;
        public Integer spellSlotsTotal;
        public Integer spellSlotsUsed;
        public Integer preparedLimit;
        public List<SpellDto> spells;

        // Optimistic concurrency
        public Long rowVersion;
    }

    public static class AttackDto {
        public String attackId;
        public String name;
        public int attackBonus;
        public String damage;
    }

    public static class SpellDto {
        public String spellId;
        public String name;
        public int level;
        public String school;
        public String castingTime;
        public String range;
        public String components;
        public String duration;
        public String description;
        public boolean prepared;
    }

    public static class CharacterResponse {
        public String characterId;
        public String roomId;
        public String ownerUserId;
        public long rowVersion;

        public String name;
        public String playerName;
        public String race;
        @SerializedName("class")
        public String characterClass;
        public int level;
        public String background;
        public String alignment;
        public int experiencePoints;

        public int strength;
        public int dexterity;
        public int constitution;
        public int intelligence;
        public int wisdom;
        public int charisma;

        public AbilityModifiers modifiers;

        public int proficiencyBonus;
        public int passivePerception;
        public Integer spellSaveDc;
        public Integer spellAttackBonus;
        public int hitDiceTotal;

        public int armorClass;
        public int initiativeBonus;
        public int speed;
        public int maxHp;
        public int currentHp;
        public int tempHp;
        public String hitDieType;
        public int hitDiceRemaining;
        public int deathSaveSuccesses;
        public int deathSaveFailures;
        public boolean inspiration;

        public int copperPieces;
        public int silverPieces;
        public int electrumPieces;
        public int goldPieces;
        public int platinumPieces;

        public String equipment;
        public String otherProficiencies;
        public String characterTraits;
        public String ideals;
        public String bonds;
        public String flaws;
        public String featuresAndTraits;

        public String eyes;
        public int age;
        public int height;
        public int weight;
        public String skin;
        public String hair;
        public String alliesAndOrganizations;
        public String backstory;
        public String treasure;
        public String additionalNotes;
        public String distinguishingMarks;

        public Map<String, SkillView> skills;
        public Map<String, SaveView> saves;
        public List<AttackDto> attacks;

        public String spellcastingClass;
        public int spellSlotsTotal;
        public int spellSlotsUsed;
        public int preparedLimit;
        public List<SpellDto> spells;
    }

    public static class AbilityModifiers {
        public int strength;
        public int dexterity;
        public int constitution;
        public int intelligence;
        public int wisdom;
        public int charisma;
    }

    public static class SkillView {
        public String level;
        public int bonus;
    }

    public static class SaveView {
        public String level;
        public int bonus;
    }
}
