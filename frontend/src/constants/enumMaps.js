export const RACE_RU_TO_EN = {
    Дварф: 'Dwarf',
    Эльф: 'Elf',
    Полурослик: 'Halfling',
    Человек: 'Human',
    Драконорождённый: 'Dragonborn',
    Гном: 'Gnome',
    Полуэльф: 'HalfElf',
    Полуорк: 'HalfOrc',
    Тифлинг: 'Tiefling',
};

export const CLASS_RU_TO_EN = {
    Варвар: 'Barbarian',
    Бард: 'Bard',
    Жрец: 'Cleric',
    Друид: 'Druid',
    Воин: 'Fighter',
    Монах: 'Monk',
    Паладин: 'Paladin',
    Следопыт: 'Ranger',
    Плут: 'Rogue',
    Чародей: 'Sorcerer',
    Колдун: 'Warlock',
    Волшебник: 'Wizard',
};

export const BACKGROUND_RU_TO_EN = {
    Аколит: 'Acolyte',
    Шарлатан: 'Charlatan',
    Преступник: 'Criminal',
    Артист: 'Entertainer',
    'Народный герой': 'FolkHero',
    'Гильдейский ремесленник': 'GuildArtisan',
    Отшельник: 'Hermit',
    Дворянин: 'Noble',
    Чужеземец: 'Outlander',
    Мудрец: 'Sage',
    Моряк: 'Sailor',
    Солдат: 'Soldier',
    Беспризорник: 'Urchin',
};

export const ALIGNMENT_RU_TO_EN = {
    'Законно-добрый': 'LawfulGood',
    'Нейтрально-добрый': 'NeutralGood',
    'Хаотично-добрый': 'ChaoticGood',
    'Законно-нейтральный': 'LawfulNeutral',
    'Истинно нейтральный': 'TrueNeutral',
    Нейтральный: 'TrueNeutral',
    'Хаотично-нейтральный': 'ChaoticNeutral',
    'Законно-злой': 'LawfulEvil',
    'Нейтрально-злой': 'NeutralEvil',
    'Хаотично-злой': 'ChaoticEvil',
};

const invert = (m) =>
    Object.fromEntries(Object.entries(m).map(([k, v]) => [v, k]));

export const RACE_EN_TO_RU = invert(RACE_RU_TO_EN);
export const CLASS_EN_TO_RU = invert(CLASS_RU_TO_EN);
export const BACKGROUND_EN_TO_RU = invert(BACKGROUND_RU_TO_EN);
export const ALIGNMENT_EN_TO_RU = {
    ...invert(ALIGNMENT_RU_TO_EN),
    TrueNeutral: 'Истинно нейтральный',
};

export const toApiRace = (ru) => RACE_RU_TO_EN[ru] ?? ru;
export const toApiClass = (ru) => CLASS_RU_TO_EN[ru] ?? ru;
export const toApiBackground = (ru) => BACKGROUND_RU_TO_EN[ru] ?? ru;
export const toApiAlignment = (ru) => ALIGNMENT_RU_TO_EN[ru] ?? ru;

export const fromApiRace = (en) => RACE_EN_TO_RU[en] ?? en;
export const fromApiClass = (en) => CLASS_EN_TO_RU[en] ?? en;
export const fromApiBackground = (en) => BACKGROUND_EN_TO_RU[en] ?? en;
export const fromApiAlignment = (en) => ALIGNMENT_EN_TO_RU[en] ?? en;
