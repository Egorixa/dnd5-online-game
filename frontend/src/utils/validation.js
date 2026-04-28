import * as yup from 'yup';

export const characterBasicSchema = yup.object({
  name: yup.string().required('Введите имя').min(1).max(50, 'Максимум 50 символов')
    .matches(/^[a-zA-Zа-яА-ЯёЁ0-9 ]+$/, 'Только буквы, цифры и пробелы'),
  playerName: yup.string().max(50, 'Максимум 50 символов'),
  race: yup.string().required('Выберите расу'),
  class: yup.string().required('Выберите класс'),
  level: yup.number().required().min(1, 'Мин. 1').max(20, 'Макс. 20').integer(),
  background: yup.string(),
  alignment: yup.string(),
  experiencePoints: yup.number().min(0).max(999999).integer(),
});

export const abilityScoreSchema = yup.number()
  .required('Обязательно')
  .min(1, 'Мин. 1')
  .max(30, 'Макс. 30')
  .integer('Целое число');

export const combatSchema = yup.object({
  armorClass: yup.number().min(1, 'Мин. 1').max(50, 'Макс. 50').integer(),
  initiativeBonus: yup.number().min(-20).max(20).integer(),
  speed: yup.number().min(0).max(200).integer(),
  hpMax: yup.number().min(1, 'Мин. 1').max(999).integer(),
  hpCurrent: yup.number().min(0).max(999).integer(),
  hpTemp: yup.number().min(0).max(999).integer(),
  hitDiceCurrent: yup.number().min(0).integer(),
});

export const attackSchema = yup.object({
  name: yup.string().required('Название').max(50),
  attackBonus: yup.number().min(-20).max(20).integer(),
  damage: yup.string().max(50),
});

export const textFieldSchema = (maxLen) =>
  yup.string().max(maxLen, `Максимум ${maxLen} символов`);

export const currencySchema = yup.number().min(0).max(999999).integer();

export const spellSchema = yup.object({
  name: yup.string().required('Название').max(60),
  level: yup.number().min(0).max(9).integer(),
  school: yup.string().max(30),
  castingTime: yup.string().max(50),
  range: yup.string().max(50),
  components: yup.string().max(50),
  duration: yup.string().max(50),
  description: yup.string().max(1000),
  prepared: yup.boolean(),
});
