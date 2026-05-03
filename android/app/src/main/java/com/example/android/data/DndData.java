package com.example.android.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Справочные данные DnD5 (Приложение Б ТЗ).
 */
public final class DndData {

    private DndData() {}

    public static final String[] RACES = {
            "Дварф", "Эльф", "Полурослик", "Человек",
            "Драконорождённый", "Гном", "Полуэльф", "Полуорк", "Тифлинг"
    };

    public static final String[] CLASSES = {
            "Варвар", "Бард", "Жрец", "Друид", "Воин", "Монах",
            "Паладин", "Следопыт", "Плут", "Чародей", "Колдун", "Волшебник"
    };

    public static final String[] BACKGROUNDS = {
            "Аколит", "Артист", "Беспризорник", "Благородный", "Гильдейский ремесленник",
            "Матрос", "Мудрец", "Народный герой", "Отшельник", "Преступник",
            "Прислужник", "Солдат", "Чужеземец", "Шарлатан"
    };

    public static final String[] ALIGNMENTS = {
            "Законопослушный добрый", "Нейтральный добрый", "Хаотичный добрый",
            "Законопослушный нейтральный", "Истинно нейтральный", "Хаотичный нейтральный",
            "Законопослушный злой", "Нейтральный злой", "Хаотичный злой"
    };

    public static final String[] HIT_DICE = {"d6", "d8", "d10", "d12"};

    public static final String[] PROFICIENCY_LEVELS = {"Нет", "Владение"};

    public static final String[] SPELL_ABILITIES = {"Инт", "Мдр", "Хар"};

    /** Кость хитов по классу (Таблица Б.5). */
    private static final Map<String, String> CLASS_HIT_DIE = new HashMap<>();

    /** Базовая характеристика заклинаний по классу (Таблица Б.6). */
    private static final Map<String, String> CLASS_SPELL_ABILITY = new HashMap<>();

    private static final List<String> CASTER_CLASSES = Arrays.asList(
            "Бард", "Жрец", "Друид", "Паладин", "Следопыт",
            "Чародей", "Колдун", "Волшебник"
    );

    static {
        CLASS_HIT_DIE.put("Варвар", "d12");
        CLASS_HIT_DIE.put("Бард", "d8");
        CLASS_HIT_DIE.put("Жрец", "d8");
        CLASS_HIT_DIE.put("Друид", "d8");
        CLASS_HIT_DIE.put("Воин", "d10");
        CLASS_HIT_DIE.put("Монах", "d8");
        CLASS_HIT_DIE.put("Паладин", "d10");
        CLASS_HIT_DIE.put("Следопыт", "d10");
        CLASS_HIT_DIE.put("Плут", "d8");
        CLASS_HIT_DIE.put("Чародей", "d6");
        CLASS_HIT_DIE.put("Колдун", "d8");
        CLASS_HIT_DIE.put("Волшебник", "d6");

        CLASS_SPELL_ABILITY.put("Бард", "Хар");
        CLASS_SPELL_ABILITY.put("Жрец", "Мдр");
        CLASS_SPELL_ABILITY.put("Друид", "Мдр");
        CLASS_SPELL_ABILITY.put("Паладин", "Хар");
        CLASS_SPELL_ABILITY.put("Следопыт", "Мдр");
        CLASS_SPELL_ABILITY.put("Чародей", "Хар");
        CLASS_SPELL_ABILITY.put("Колдун", "Хар");
        CLASS_SPELL_ABILITY.put("Волшебник", "Инт");
    }

    public static String hitDieForClass(String cls) {
        return CLASS_HIT_DIE.getOrDefault(cls, "d8");
    }

    public static String spellAbilityForClass(String cls) {
        return CLASS_SPELL_ABILITY.getOrDefault(cls, "");
    }

    public static boolean isSpellcaster(String cls) {
        return CASTER_CLASSES.contains(cls);
    }
}
