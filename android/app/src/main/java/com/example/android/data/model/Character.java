package com.example.android.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "characters")
public class Character {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int userId;

    public String characterName = "";
    public String playerName = "";
    public String race = "";
    public String characterClass = "";
    public int level = 1;
    public String background = "";
    public String alignment = "";
    public int experiencePoints = 0;

    public int strength = 10;
    public int dexterity = 10;
    public int constitution = 10;
    public int intelligence = 10;
    public int wisdom = 10;
    public int charisma = 10;

    public int armorClass = 10;
    public int initiativeBonus = 0;
    public int speed = 30;
    public int maxHp = 1;
    public int currentHp = 1;
    public int tempHp = 0;
    public String hitDie = "d8";
    public int hitDieCurrent = 1;
    public int deathSaveSuccesses = 0;
    public int deathSaveFailures = 0;

    public boolean inspiration = false;

    public int savingThrowStr = 0;
    public int savingThrowDex = 0;
    public int savingThrowCon = 0;
    public int savingThrowInt = 0;
    public int savingThrowWis = 0;
    public int savingThrowCha = 0;

    public int skillAcrobatics = 0;
    public int skillAnimalHandling = 0;
    public int skillArcana = 0;
    public int skillAthletics = 0;
    public int skillDeception = 0;
    public int skillHistory = 0;
    public int skillInsight = 0;
    public int skillIntimidation = 0;
    public int skillInvestigation = 0;
    public int skillMedicine = 0;
    public int skillNature = 0;
    public int skillPerception = 0;
    public int skillPerformance = 0;
    public int skillPersuasion = 0;
    public int skillReligion = 0;
    public int skillSleightOfHand = 0;
    public int skillStealth = 0;
    public int skillSurvival = 0;

    public String equipment = "";
    public String otherProficiencies = "";
    public int copperPieces = 0;
    public int silverPieces = 0;
    public int electrumPieces = 0;
    public int goldPieces = 0;
    public int platinumPieces = 0;

    public String attacksJson = "[]";

    public String personalityTraits = "";
    public String ideals = "";
    public String bonds = "";
    public String flaws = "";
    public String featuresAndTraits = "";

    public String eyes = "";
    public int age = 0;
    public int height = 0;
    public int weight = 0;
    public String skin = "";
    public String hair = "";
    public String alliesAndOrganizations = "";
    public String backstory = "";
    public String treasure = "";
    public String additionalNotes = "";
    public String distinguishingMarks = "";

    public String spellcastingClass = "";
    public String spellcastingAbility = "";

    public String spellSlotsJson = "{}";

    public String spellsJson = "[]";

    public long createdAt = System.currentTimeMillis();
    public long updatedAt = System.currentTimeMillis();

    public static int modifier(int value) {
        return (int) Math.floor((value - 10) / 2.0);
    }

    public static int proficiencyBonus(int level) {
        return (int) Math.ceil(level / 4.0) + 1;
    }

    public int passivePerception() {
        int prof = (skillPerception == 1) ? proficiencyBonus(level) : 0;
        return 10 + modifier(wisdom) + prof;
    }

    public int initiative() {
        return modifier(dexterity) + initiativeBonus;
    }

    public int spellSaveDC() {
        int spellMod = spellAbilityModifier();
        return 8 + proficiencyBonus(level) + spellMod;
    }

    public int spellAttackBonus() {
        return proficiencyBonus(level) + spellAbilityModifier();
    }

    private int spellAbilityModifier() {
        switch (spellcastingAbility) {
            case "Инт": return modifier(intelligence);
            case "Мдр": return modifier(wisdom);
            case "Хар": return modifier(charisma);
            default: return 0;
        }
    }
}
