export const CLASSES = [
  { value: 'barbarian', label: 'Варвар', labelEn: 'Barbarian', hitDie: 'd12', spellcasting: null, spellAbility: null },
  { value: 'bard', label: 'Бард', labelEn: 'Bard', hitDie: 'd8', spellcasting: 'full', spellAbility: 'charisma' },
  { value: 'cleric', label: 'Жрец', labelEn: 'Cleric', hitDie: 'd8', spellcasting: 'full', spellAbility: 'wisdom' },
  { value: 'druid', label: 'Друид', labelEn: 'Druid', hitDie: 'd8', spellcasting: 'full', spellAbility: 'wisdom' },
  { value: 'fighter', label: 'Воин', labelEn: 'Fighter', hitDie: 'd10', spellcasting: null, spellAbility: null },
  { value: 'monk', label: 'Монах', labelEn: 'Monk', hitDie: 'd8', spellcasting: null, spellAbility: null },
  { value: 'paladin', label: 'Паладин', labelEn: 'Paladin', hitDie: 'd10', spellcasting: 'half', spellAbility: 'charisma' },
  { value: 'ranger', label: 'Следопыт', labelEn: 'Ranger', hitDie: 'd10', spellcasting: 'half', spellAbility: 'wisdom' },
  { value: 'rogue', label: 'Плут', labelEn: 'Rogue', hitDie: 'd8', spellcasting: null, spellAbility: null },
  { value: 'sorcerer', label: 'Чародей', labelEn: 'Sorcerer', hitDie: 'd6', spellcasting: 'full', spellAbility: 'charisma' },
  { value: 'warlock', label: 'Колдун', labelEn: 'Warlock', hitDie: 'd8', spellcasting: 'pact', spellAbility: 'charisma' },
  { value: 'wizard', label: 'Волшебник', labelEn: 'Wizard', hitDie: 'd6', spellcasting: 'full', spellAbility: 'intelligence' },
];

export const getClassByValue = (value) => CLASSES.find((c) => c.value === value);
