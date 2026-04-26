package com.example.android.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Модель персонажа DnD5 — хранит все поля листа персонажа согласно ТЗ.
 */
@Entity(tableName = "characters")
public class Character {

    @PrimaryKey(autoGenerate = true)
    public int id;

    // Владелец персонажа
    public int userId;

    // ── Основные сведения ──────────────────────────────────────────────────
    public String characterName = "";   // 1–50 символов
    public String playerName = "";      // 1–50 символов
    public String race = "";            // из списка Б.1
    public String characterClass = "";  // из списка Б.2
    public int level = 1;              // 1–20
    public String background = "";      // из списка
    public String alignment = "";       // из списка
    public int experiencePoints = 0;   // 0–999999

    // ── Характеристики ────────────────────────────────────────────────────
    public int strength = 10;          // 1–30
    public int dexterity = 10;
    public int constitution = 10;
    public int intelligence = 10;
    public int wisdom = 10;
    public int charisma = 10;

    // ── Боевые параметры ──────────────────────────────────────────────────
    public int armorClass = 10;        // 1–50
    public int initiativeBonus = 0;    // −20…+20 (доп. бонус сверх мод. ловкости)
    public int speed = 30;             // 0–200 футов
    public int maxHp = 1;              // 1–999
    public int currentHp = 1;          // 0–999
    public int tempHp = 0;             // 0–999
    public String hitDie = "d8";       // d6/d8/d10/d12
    public int hitDieCurrent = 1;      // 0…level
    public int deathSaveSuccesses = 0; // 0–3
    public int deathSaveFailures = 0;  // 0–3

    // ── Вдохновение и мастерство ──────────────────────────────────────────
    public boolean inspiration = false;
    // proficiencyBonus рассчитывается автоматически по уровню

    // ── Спасброски (владение: 0=нет, 1=владение) ─────────────────────────
    public int savingThrowStr = 0;
    public int savingThrowDex = 0;
    public int savingThrowCon = 0;
    public int savingThrowInt = 0;
    public int savingThrowWis = 0;
    public int savingThrowCha = 0;

    // ── Навыки (0=нет, 1=владение) ────────────────────────────────────────
    public int skillAcrobatics = 0;       // Акробатика (Лов)
    public int skillAnimalHandling = 0;   // Уход за животными (Мдр)
    public int skillArcana = 0;           // Магия (Инт)
    public int skillAthletics = 0;        // Атлетика (Сил)
    public int skillDeception = 0;        // Обман (Хар)
    public int skillHistory = 0;          // История (Инт)
    public int skillInsight = 0;          // Проницательность (Мдр)
    public int skillIntimidation = 0;     // Запугивание (Хар)
    public int skillInvestigation = 0;    // Анализ (Инт)
    public int skillMedicine = 0;         // Медицина (Мдр)
    public int skillNature = 0;           // Природа (Инт)
    public int skillPerception = 0;       // Внимательность (Мдр)
    public int skillPerformance = 0;      // Выступление (Хар)
    public int skillPersuasion = 0;       // Убеждение (Хар)
    public int skillReligion = 0;         // Религия (Инт)
    public int skillSleightOfHand = 0;    // Ловкость рук (Лов)
    public int skillStealth = 0;          // Скрытность (Лов)
    public int skillSurvival = 0;         // Выживание (Мдр)

    // ── Снаряжение и валюта ───────────────────────────────────────────────
    public String equipment = "";          // до 2000 символов
    public String otherProficiencies = ""; // до 1000 символов
    public int copperPieces = 0;
    public int silverPieces = 0;
    public int electrumPieces = 0;
    public int goldPieces = 0;
    public int platinumPieces = 0;

    // ── Атаки и заклинания (JSON-строка) ─────────────────────────────────
    // Формат: [{"name":"...","attackBonus":0,"damage":"..."}]
    public String attacksJson = "[]";      // до 20 строк

    // ── Черты личности ────────────────────────────────────────────────────
    public String personalityTraits = "";  // до 1500
    public String ideals = "";             // до 1500
    public String bonds = "";              // до 1500
    public String flaws = "";              // до 1500
    public String featuresAndTraits = "";  // до 3000

    // ── Внешность и предыстория ───────────────────────────────────────────
    public String eyes = "";               // 0–30
    public int age = 0;                    // 0–999
    public int height = 0;                 // 0–999
    public int weight = 0;                 // 0–999
    public String skin = "";               // 0–30
    public String hair = "";               // 0–30
    public String alliesAndOrganizations = ""; // до 1500
    public String backstory = "";          // до 3000
    public String treasure = "";           // до 1500
    public String additionalNotes = "";    // до 1500
    public String distinguishingMarks = ""; // до 1500

    // ── Заклинания ────────────────────────────────────────────────────────
    public String spellcastingClass = "";
    public String spellcastingAbility = ""; // Инт / Мдр / Хар
    // spellSaveDC и spellAttackBonus рассчитываются автоматически
    // Ячейки заклинаний по уровням (всего/использовано) — JSON
    public String spellSlotsJson = "{}";
    // Список заклинаний — JSON
    // [{"name":"...","level":0,"school":"...","castingTime":"...","range":"...","components":"...","duration":"...","description":"...","prepared":false}]
    public String spellsJson = "[]";

    // ── Метаданные ────────────────────────────────────────────────────────
    public long createdAt = System.currentTimeMillis();
    public long updatedAt = System.currentTimeMillis();

    // ── Вычисляемые методы ────────────────────────────────────────────────

    /** Модификатор характеристики: floor((value - 10) / 2) */
    public static int modifier(int value) {
        return (int) Math.floor((value - 10) / 2.0);
    }

    /** Бонус мастерства по уровню */
    public static int proficiencyBonus(int level) {
        return (int) Math.ceil(level / 4.0) + 1;
    }

    /** Пассивная внимательность */
    public int passivePerception() {
        int prof = (skillPerception == 1) ? proficiencyBonus(level) : 0;
        return 10 + modifier(wisdom) + prof;
    }

    /** Инициатива = мод. ловкости + доп. бонус */
    public int initiative() {
        return modifier(dexterity) + initiativeBonus;
    }

    /** Spell Save DC */
    public int spellSaveDC() {
        int spellMod = spellAbilityModifier();
        return 8 + proficiencyBonus(level) + spellMod;
    }

    /** Spell Attack Bonus */
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
