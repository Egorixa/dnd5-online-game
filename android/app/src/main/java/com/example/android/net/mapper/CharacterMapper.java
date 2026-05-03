package com.example.android.net.mapper;

import android.text.TextUtils;

import com.example.android.data.model.Character;
import com.example.android.net.dto.CharacterDtos;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Двусторонний маппинг локальной модели Character (русские названия,
 * 0/1 для спасбросков, 0/1/2 для навыков) и серверных DTO с enum-ключами.
 *
 * Backend поддерживает только 2 уровня владения (None/Proficient).
 * Уровень UI «Компетентность» (2) маппится в Proficient при отправке.
 */
public final class CharacterMapper {

    private CharacterMapper() {}

    // ── Названия (RU ↔ enum-ключ) ──────────────────────────────────────────

    private static final Map<String, String> RACE_RU_TO_KEY = new HashMap<>();
    private static final Map<String, String> RACE_KEY_TO_RU = new HashMap<>();
    private static final Map<String, String> CLASS_RU_TO_KEY = new HashMap<>();
    private static final Map<String, String> CLASS_KEY_TO_RU = new HashMap<>();
    private static final Map<String, String> BG_RU_TO_KEY = new HashMap<>();
    private static final Map<String, String> BG_KEY_TO_RU = new HashMap<>();
    private static final Map<String, String> ALIGN_RU_TO_KEY = new HashMap<>();
    private static final Map<String, String> ALIGN_KEY_TO_RU = new HashMap<>();

    static {
        race("Дварф", CharacterDtos.Race.DWARF);
        race("Эльф", CharacterDtos.Race.ELF);
        race("Полурослик", CharacterDtos.Race.HALFLING);
        race("Человек", CharacterDtos.Race.HUMAN);
        race("Драконорождённый", CharacterDtos.Race.DRAGONBORN);
        race("Гном", CharacterDtos.Race.GNOME);
        race("Полуэльф", CharacterDtos.Race.HALF_ELF);
        race("Полуорк", CharacterDtos.Race.HALF_ORC);
        race("Тифлинг", CharacterDtos.Race.TIEFLING);

        cls("Варвар", CharacterDtos.CharacterClass.BARBARIAN);
        cls("Бард", CharacterDtos.CharacterClass.BARD);
        cls("Жрец", CharacterDtos.CharacterClass.CLERIC);
        cls("Друид", CharacterDtos.CharacterClass.DRUID);
        cls("Воин", CharacterDtos.CharacterClass.FIGHTER);
        cls("Монах", CharacterDtos.CharacterClass.MONK);
        cls("Паладин", CharacterDtos.CharacterClass.PALADIN);
        cls("Следопыт", CharacterDtos.CharacterClass.RANGER);
        cls("Плут", CharacterDtos.CharacterClass.ROGUE);
        cls("Чародей", CharacterDtos.CharacterClass.SORCERER);
        cls("Колдун", CharacterDtos.CharacterClass.WARLOCK);
        cls("Волшебник", CharacterDtos.CharacterClass.WIZARD);

        bg("Аколит", CharacterDtos.Background.ACOLYTE);
        bg("Артист", CharacterDtos.Background.ENTERTAINER);
        bg("Беспризорник", CharacterDtos.Background.URCHIN);
        bg("Благородный", CharacterDtos.Background.NOBLE);
        bg("Гильдейский ремесленник", CharacterDtos.Background.GUILD_ARTISAN);
        bg("Матрос", CharacterDtos.Background.SAILOR);
        bg("Мудрец", CharacterDtos.Background.SAGE);
        bg("Народный герой", CharacterDtos.Background.FOLK_HERO);
        bg("Отшельник", CharacterDtos.Background.HERMIT);
        bg("Преступник", CharacterDtos.Background.CRIMINAL);
        bg("Прислужник", CharacterDtos.Background.ACOLYTE);
        bg("Солдат", CharacterDtos.Background.SOLDIER);
        bg("Чужеземец", CharacterDtos.Background.OUTLANDER);
        bg("Шарлатан", CharacterDtos.Background.CHARLATAN);

        align("Законопослушный добрый", CharacterDtos.Alignment.LAWFUL_GOOD);
        align("Нейтральный добрый", CharacterDtos.Alignment.NEUTRAL_GOOD);
        align("Хаотичный добрый", CharacterDtos.Alignment.CHAOTIC_GOOD);
        align("Законопослушный нейтральный", CharacterDtos.Alignment.LAWFUL_NEUTRAL);
        align("Истинно нейтральный", CharacterDtos.Alignment.TRUE_NEUTRAL);
        align("Хаотичный нейтральный", CharacterDtos.Alignment.CHAOTIC_NEUTRAL);
        align("Законопослушный злой", CharacterDtos.Alignment.LAWFUL_EVIL);
        align("Нейтральный злой", CharacterDtos.Alignment.NEUTRAL_EVIL);
        align("Хаотичный злой", CharacterDtos.Alignment.CHAOTIC_EVIL);
    }

    private static void race(String ru, String key) {
        RACE_RU_TO_KEY.put(ru, key); RACE_KEY_TO_RU.put(key, ru);
    }
    private static void cls(String ru, String key) {
        CLASS_RU_TO_KEY.put(ru, key); CLASS_KEY_TO_RU.put(key, ru);
    }
    private static void bg(String ru, String key) {
        BG_RU_TO_KEY.put(ru, key);
        if (!BG_KEY_TO_RU.containsKey(key)) BG_KEY_TO_RU.put(key, ru);
    }
    private static void align(String ru, String key) {
        ALIGN_RU_TO_KEY.put(ru, key); ALIGN_KEY_TO_RU.put(key, ru);
    }

    public static String raceToKey(String ru) { return RACE_RU_TO_KEY.get(ru); }
    public static String raceFromKey(String key) {
        return key == null ? "" : RACE_KEY_TO_RU.getOrDefault(key, "");
    }
    public static String classToKey(String ru) { return CLASS_RU_TO_KEY.get(ru); }
    public static String classFromKey(String key) {
        return key == null ? "" : CLASS_KEY_TO_RU.getOrDefault(key, "");
    }
    public static String bgToKey(String ru) { return BG_RU_TO_KEY.get(ru); }
    public static String bgFromKey(String key) {
        return key == null ? "" : BG_KEY_TO_RU.getOrDefault(key, "");
    }
    public static String alignToKey(String ru) { return ALIGN_RU_TO_KEY.get(ru); }
    public static String alignFromKey(String key) {
        return key == null ? "" : ALIGN_KEY_TO_RU.getOrDefault(key, "");
    }

    // ── Skills ─────────────────────────────────────────────────────────────

    /** Skill keys в порядке полей Character.skill* (как в SKILL_NAMES). */
    public static final String[] SKILL_KEYS_IN_ORDER = {
            CharacterDtos.Skill.ACROBATICS,
            CharacterDtos.Skill.ANIMAL_HANDLING,
            CharacterDtos.Skill.ARCANA,
            CharacterDtos.Skill.ATHLETICS,
            CharacterDtos.Skill.DECEPTION,
            CharacterDtos.Skill.HISTORY,
            CharacterDtos.Skill.INSIGHT,
            CharacterDtos.Skill.INTIMIDATION,
            CharacterDtos.Skill.INVESTIGATION,
            CharacterDtos.Skill.MEDICINE,
            CharacterDtos.Skill.NATURE,
            CharacterDtos.Skill.PERCEPTION,
            CharacterDtos.Skill.PERFORMANCE,
            CharacterDtos.Skill.PERSUASION,
            CharacterDtos.Skill.RELIGION,
            CharacterDtos.Skill.SLEIGHT_OF_HAND,
            CharacterDtos.Skill.STEALTH,
            CharacterDtos.Skill.SURVIVAL,
    };

    public static String profLevelToKey(int level) {
        return level >= 1
                ? CharacterDtos.ProficiencyLevel.PROFICIENT
                : CharacterDtos.ProficiencyLevel.NONE;
    }

    public static int profLevelFromKey(String key) {
        return CharacterDtos.ProficiencyLevel.PROFICIENT.equalsIgnoreCase(key) ? 1 : 0;
    }

    // ── Local Character ↔ Upsert request ──────────────────────────────────

    /** Локальная сущность → запрос на сервер. */
    public static CharacterDtos.CharacterUpsertRequest toUpsert(Character c) {
        CharacterDtos.CharacterUpsertRequest r = new CharacterDtos.CharacterUpsertRequest();
        r.name = nullIfEmpty(c.characterName);
        r.playerName = c.playerName;
        r.race = raceToKey(c.race);
        r.characterClass = classToKey(c.characterClass);
        r.level = c.level;
        r.background = bgToKey(c.background);
        r.alignment = alignToKey(c.alignment);
        r.experiencePoints = c.experiencePoints;

        r.strength = c.strength;
        r.dexterity = c.dexterity;
        r.constitution = c.constitution;
        r.intelligence = c.intelligence;
        r.wisdom = c.wisdom;
        r.charisma = c.charisma;

        r.armorClass = c.armorClass;
        r.initiativeBonus = c.initiativeBonus;
        r.speed = c.speed;
        r.maxHp = c.maxHp;
        r.currentHp = c.currentHp;
        r.tempHp = c.tempHp;
        r.hitDieType = TextUtils.isEmpty(c.hitDie) ? null : c.hitDie;
        r.hitDiceRemaining = c.hitDieCurrent;
        r.deathSaveSuccesses = c.deathSaveSuccesses;
        r.deathSaveFailures = c.deathSaveFailures;
        r.inspiration = c.inspiration;

        r.copperPieces = c.copperPieces;
        r.silverPieces = c.silverPieces;
        r.electrumPieces = c.electrumPieces;
        r.goldPieces = c.goldPieces;
        r.platinumPieces = c.platinumPieces;

        r.equipment = c.equipment;
        r.otherProficiencies = c.otherProficiencies;
        r.characterTraits = c.personalityTraits;
        r.ideals = c.ideals;
        r.bonds = c.bonds;
        r.flaws = c.flaws;
        r.featuresAndTraits = c.featuresAndTraits;

        r.eyes = c.eyes;
        r.age = c.age;
        r.height = c.height;
        r.weight = c.weight;
        r.skin = c.skin;
        r.hair = c.hair;
        r.alliesAndOrganizations = c.alliesAndOrganizations;
        r.backstory = c.backstory;
        r.treasure = c.treasure;
        r.additionalNotes = c.additionalNotes;
        r.distinguishingMarks = c.distinguishingMarks;

        // Saves
        Map<String, String> saves = new LinkedHashMap<>();
        saves.put(CharacterDtos.Ability.STRENGTH, profLevelToKey(c.savingThrowStr));
        saves.put(CharacterDtos.Ability.DEXTERITY, profLevelToKey(c.savingThrowDex));
        saves.put(CharacterDtos.Ability.CONSTITUTION, profLevelToKey(c.savingThrowCon));
        saves.put(CharacterDtos.Ability.INTELLIGENCE, profLevelToKey(c.savingThrowInt));
        saves.put(CharacterDtos.Ability.WISDOM, profLevelToKey(c.savingThrowWis));
        saves.put(CharacterDtos.Ability.CHARISMA, profLevelToKey(c.savingThrowCha));
        r.saveProficiencies = saves;

        // Skills
        int[] skillVals = {
                c.skillAcrobatics, c.skillAnimalHandling, c.skillArcana, c.skillAthletics,
                c.skillDeception, c.skillHistory, c.skillInsight, c.skillIntimidation,
                c.skillInvestigation, c.skillMedicine, c.skillNature, c.skillPerception,
                c.skillPerformance, c.skillPersuasion, c.skillReligion,
                c.skillSleightOfHand, c.skillStealth, c.skillSurvival
        };
        Map<String, String> skills = new LinkedHashMap<>();
        for (int i = 0; i < SKILL_KEYS_IN_ORDER.length && i < skillVals.length; i++) {
            skills.put(SKILL_KEYS_IN_ORDER[i], profLevelToKey(skillVals[i]));
        }
        r.skillProficiencies = skills;

        // Spellcasting
        if (!TextUtils.isEmpty(c.spellcastingClass)) {
            r.spellcastingClass = classToKey(c.spellcastingClass);
        }

        // Attacks
        r.attacks = parseAttacksJson(c.attacksJson);

        if (c.rowVersion > 0) {
            r.rowVersion = c.rowVersion;
        }
        return r;
    }

    /** Серверный ответ → локальная сущность (создание/обновление кэша). */
    public static Character fromResponse(CharacterDtos.CharacterResponse resp,
                                         Character existing,
                                         int localUserId) {
        Character c = existing != null ? existing : new Character();
        c.userId = localUserId;
        c.serverCharacterId = resp.characterId == null ? "" : resp.characterId;
        c.rowVersion = resp.rowVersion;

        c.characterName = nz(resp.name);
        c.playerName = nz(resp.playerName);
        c.race = raceFromKey(resp.race);
        c.characterClass = classFromKey(resp.characterClass);
        c.background = bgFromKey(resp.background);
        c.alignment = alignFromKey(resp.alignment);
        c.level = resp.level <= 0 ? 1 : resp.level;
        c.experiencePoints = resp.experiencePoints;

        c.strength = resp.strength;
        c.dexterity = resp.dexterity;
        c.constitution = resp.constitution;
        c.intelligence = resp.intelligence;
        c.wisdom = resp.wisdom;
        c.charisma = resp.charisma;

        c.armorClass = resp.armorClass;
        c.initiativeBonus = resp.initiativeBonus;
        c.speed = resp.speed;
        c.maxHp = resp.maxHp;
        c.currentHp = resp.currentHp;
        c.tempHp = resp.tempHp;
        c.hitDie = TextUtils.isEmpty(resp.hitDieType) ? "d8" : resp.hitDieType;
        c.hitDieCurrent = resp.hitDiceRemaining;
        c.deathSaveSuccesses = resp.deathSaveSuccesses;
        c.deathSaveFailures = resp.deathSaveFailures;
        c.inspiration = resp.inspiration;

        c.copperPieces = resp.copperPieces;
        c.silverPieces = resp.silverPieces;
        c.electrumPieces = resp.electrumPieces;
        c.goldPieces = resp.goldPieces;
        c.platinumPieces = resp.platinumPieces;

        c.equipment = nz(resp.equipment);
        c.otherProficiencies = nz(resp.otherProficiencies);
        c.personalityTraits = nz(resp.characterTraits);
        c.ideals = nz(resp.ideals);
        c.bonds = nz(resp.bonds);
        c.flaws = nz(resp.flaws);
        c.featuresAndTraits = nz(resp.featuresAndTraits);

        c.eyes = nz(resp.eyes);
        c.age = resp.age;
        c.height = resp.height;
        c.weight = resp.weight;
        c.skin = nz(resp.skin);
        c.hair = nz(resp.hair);
        c.alliesAndOrganizations = nz(resp.alliesAndOrganizations);
        c.backstory = nz(resp.backstory);
        c.treasure = nz(resp.treasure);
        c.additionalNotes = nz(resp.additionalNotes);
        c.distinguishingMarks = nz(resp.distinguishingMarks);

        c.spellcastingClass = classFromKey(resp.spellcastingClass);

        // Saves
        if (resp.saves != null) {
            c.savingThrowStr = profFromMap(resp.saves, CharacterDtos.Ability.STRENGTH);
            c.savingThrowDex = profFromMap(resp.saves, CharacterDtos.Ability.DEXTERITY);
            c.savingThrowCon = profFromMap(resp.saves, CharacterDtos.Ability.CONSTITUTION);
            c.savingThrowInt = profFromMap(resp.saves, CharacterDtos.Ability.INTELLIGENCE);
            c.savingThrowWis = profFromMap(resp.saves, CharacterDtos.Ability.WISDOM);
            c.savingThrowCha = profFromMap(resp.saves, CharacterDtos.Ability.CHARISMA);
        }

        // Skills
        if (resp.skills != null) {
            c.skillAcrobatics = profFromMap(resp.skills, CharacterDtos.Skill.ACROBATICS);
            c.skillAnimalHandling = profFromMap(resp.skills, CharacterDtos.Skill.ANIMAL_HANDLING);
            c.skillArcana = profFromMap(resp.skills, CharacterDtos.Skill.ARCANA);
            c.skillAthletics = profFromMap(resp.skills, CharacterDtos.Skill.ATHLETICS);
            c.skillDeception = profFromMap(resp.skills, CharacterDtos.Skill.DECEPTION);
            c.skillHistory = profFromMap(resp.skills, CharacterDtos.Skill.HISTORY);
            c.skillInsight = profFromMap(resp.skills, CharacterDtos.Skill.INSIGHT);
            c.skillIntimidation = profFromMap(resp.skills, CharacterDtos.Skill.INTIMIDATION);
            c.skillInvestigation = profFromMap(resp.skills, CharacterDtos.Skill.INVESTIGATION);
            c.skillMedicine = profFromMap(resp.skills, CharacterDtos.Skill.MEDICINE);
            c.skillNature = profFromMap(resp.skills, CharacterDtos.Skill.NATURE);
            c.skillPerception = profFromMap(resp.skills, CharacterDtos.Skill.PERCEPTION);
            c.skillPerformance = profFromMap(resp.skills, CharacterDtos.Skill.PERFORMANCE);
            c.skillPersuasion = profFromMap(resp.skills, CharacterDtos.Skill.PERSUASION);
            c.skillReligion = profFromMap(resp.skills, CharacterDtos.Skill.RELIGION);
            c.skillSleightOfHand = profFromMap(resp.skills, CharacterDtos.Skill.SLEIGHT_OF_HAND);
            c.skillStealth = profFromMap(resp.skills, CharacterDtos.Skill.STEALTH);
            c.skillSurvival = profFromMap(resp.skills, CharacterDtos.Skill.SURVIVAL);
        }

        // Attacks
        c.attacksJson = attacksToJson(resp.attacks);

        c.updatedAt = System.currentTimeMillis();
        return c;
    }

    private static int profFromMap(Map<String, ?> map, String key) {
        Object v = map.get(key);
        if (v == null) return 0;
        // SkillView/SaveView имеют поле level
        try {
            java.lang.reflect.Field f = v.getClass().getField("level");
            Object lvl = f.get(v);
            return profLevelFromKey(lvl == null ? null : lvl.toString());
        } catch (Throwable ignored) {
            return profLevelFromKey(v.toString());
        }
    }

    private static List<CharacterDtos.AttackDto> parseAttacksJson(String json) {
        if (TextUtils.isEmpty(json)) return new java.util.ArrayList<>();
        try {
            java.lang.reflect.Type t = new com.google.gson.reflect.TypeToken<
                    List<AttackJson>>() {}.getType();
            List<AttackJson> raw = new com.google.gson.Gson().fromJson(json, t);
            List<CharacterDtos.AttackDto> out = new java.util.ArrayList<>();
            if (raw == null) return out;
            for (AttackJson a : raw) {
                CharacterDtos.AttackDto d = new CharacterDtos.AttackDto();
                d.name = nz(a.name);
                d.attackBonus = a.attackBonus;
                d.damage = nz(a.damage);
                out.add(d);
            }
            return out;
        } catch (Throwable t) {
            return new java.util.ArrayList<>();
        }
    }

    private static String attacksToJson(List<CharacterDtos.AttackDto> attacks) {
        if (attacks == null) return "[]";
        List<AttackJson> out = new java.util.ArrayList<>();
        for (CharacterDtos.AttackDto a : attacks) {
            AttackJson j = new AttackJson();
            j.name = nz(a.name);
            j.attackBonus = a.attackBonus;
            j.damage = nz(a.damage);
            out.add(j);
        }
        return new com.google.gson.Gson().toJson(out);
    }

    private static class AttackJson {
        String name = "";
        int attackBonus = 0;
        String damage = "";
    }

    private static String nullIfEmpty(String s) {
        return TextUtils.isEmpty(s) ? null : s;
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }
}
