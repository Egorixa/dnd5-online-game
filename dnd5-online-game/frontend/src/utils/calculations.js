import { getProficiencyBonus } from '../constants/proficiencyBonus';
import { getClassByValue } from '../constants/classes';

export const getModifier = (score) => Math.floor((score - 10) / 2);

export const formatModifier = (mod) => (mod >= 0 ? `+${mod}` : `${mod}`);

const profMultiplier = (profLevel) => {
  if (profLevel === 'Expertise') return 2;
  if (profLevel === 'Proficient' || profLevel === true) return 1;
  return 0;
};

export const getSavingThrowBonus = (abilityScore, level, profLevel) => {
  const mod = getModifier(abilityScore);
  return mod + profMultiplier(profLevel) * getProficiencyBonus(level);
};

export const getSkillBonus = (abilityScore, level, profLevel) => {
  const mod = getModifier(abilityScore);
  return mod + profMultiplier(profLevel) * getProficiencyBonus(level);
};

export const getPassivePerception = (wisdomScore, level, perceptionLevel) => {
  return 10 + getSkillBonus(wisdomScore, level, perceptionLevel);
};

export const getSpellSaveDC = (level, spellAbilityScore) => {
  return 8 + getProficiencyBonus(level) + getModifier(spellAbilityScore);
};

export const getSpellAttackBonus = (level, spellAbilityScore) => {
  return getProficiencyBonus(level) + getModifier(spellAbilityScore);
};

export const getHitDie = (classValue) => {
  const cls = getClassByValue(classValue);
  return cls ? cls.hitDie : 'd8';
};

export const canCastSpells = (classValue) => {
  const cls = getClassByValue(classValue);
  return cls ? cls.spellcasting !== null : false;
};

export const getSpellAbility = (classValue) => {
  const cls = getClassByValue(classValue);
  return cls ? cls.spellAbility : null;
};

export const getDefaultCharacter = () => ({
  name: '',
  playerName: '',
  race: '',
  class: '',
  level: 1,
  background: '',
  alignment: '',
  experiencePoints: 0,

  strength: 10,
  dexterity: 10,
  constitution: 10,
  intelligence: 10,
  wisdom: 10,
  charisma: 10,

  savingThrows: {},

  skills: {},

  inspiration: false,
  armorClass: 10,
  initiativeBonus: 0,
  speed: 30,
  hpMax: 10,
  hpCurrent: 10,
  hpTemp: 0,
  hitDiceTotal: 1,
  hitDiceCurrent: 1,
  deathSavesSuccesses: 0,
  deathSavesFailures: 0,

  equipment: '',
  proficienciesAndLanguages: '',
  copper: 0,
  silver: 0,
  electrum: 0,
  gold: 0,
  platinum: 0,

  attacks: [],

  personalityTraits: '',
  ideals: '',
  bonds: '',
  flaws: '',
  featuresAndTraits: '',

  eyes: '',
  age: 0,
  height: 0,
  weight: 0,
  skin: '',
  hair: '',
  allies: '',
  backstory: '',
  treasure: '',
  additionalNotes: '',
  distinguishingMarks: '',

  spellcastingClass: '',
  spellSlots: {},
  spells: [],
});
