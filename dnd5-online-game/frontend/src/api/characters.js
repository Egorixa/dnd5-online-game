import client from './client';

const toPascalNoSep = (s) => {
  if (typeof s !== 'string' || !s) return s;
  return s.split('-').map((p) => p.charAt(0).toUpperCase() + p.slice(1).toLowerCase()).join('');
};

const toKebabLower = (s) => {
  if (typeof s !== 'string' || !s) return s;
  return s.replace(/([a-z0-9])([A-Z])/g, '$1-$2').toLowerCase();
};

const ENUM_FIELDS = ['race', 'characterClass', 'background', 'alignment', 'spellcastingClass'];

const FIELD_MAP_OUT = {
  hpMax: 'maxHp',
  hpCurrent: 'currentHp',
  hpTemp: 'tempHp',
  hitDiceCurrent: 'hitDiceRemaining',
  deathSavesSuccesses: 'deathSaveSuccesses',
  deathSavesFailures: 'deathSaveFailures',
  copper: 'copperPieces',
  silver: 'silverPieces',
  electrum: 'electrumPieces',
  gold: 'goldPieces',
  platinum: 'platinumPieces',
  proficienciesAndLanguages: 'otherProficiencies',
  allies: 'alliesAndOrganizations',
  personalityTraits: 'characterTraits',
};
const FIELD_MAP_IN = Object.fromEntries(
  Object.entries(FIELD_MAP_OUT).map(([fr, be]) => [be, fr]),
);

const renameKeys = (obj, map) => {
  if (!obj || typeof obj !== 'object') return obj;
  const out = {};
  for (const [k, v] of Object.entries(obj)) {
    out[map[k] || k] = v;
  }
  return out;
};

const VALID_LEVELS = new Set(['None', 'Proficient', 'Expertise']);
const normalizeLevel = (v) => {
  if (v === true) return 'Proficient';
  if (typeof v === 'string' && VALID_LEVELS.has(v)) return v;
  return 'None';
};

const proficienciesFromLevelMap = (levelMap) => {
  if (!levelMap || typeof levelMap !== 'object') return undefined;
  const out = {};
  for (const [k, v] of Object.entries(levelMap)) {
    const lvl = normalizeLevel(v);
    if (lvl !== 'None') out[k] = lvl;
  }
  return out;
};

const proficienciesToLevelMap = (profMap) => {
  if (!profMap || typeof profMap !== 'object') return {};
  const out = {};
  for (const [k, v] of Object.entries(profMap)) {
    const lvl = normalizeLevel(v);
    if (lvl !== 'None') out[k] = lvl;
  }
  return out;
};

const viewMapToLevelMap = (viewMap) => {
  if (!viewMap || typeof viewMap !== 'object') return {};
  const out = {};
  for (const [k, v] of Object.entries(viewMap)) {
    const level = normalizeLevel(v?.level ?? v);
    if (level !== 'None') out[toKebabLower(k)] = level;
  }
  return out;
};

const encodeSpellSlots = (slots) => {
  if (!slots || typeof slots !== 'object') return undefined;
  const out = {};
  for (const [k, v] of Object.entries(slots)) {
    const lvl = parseInt(k);
    if (!Number.isFinite(lvl) || lvl < 1 || lvl > 9) continue;
    const total = Math.min(99, Math.max(0, parseInt(v?.total) || 0));
    const used = Math.min(total, Math.max(0, parseInt(v?.used) || 0));
    if (total === 0 && used === 0) continue;
    out[lvl] = { total, used };
  }
  return out;
};

const decodeSpellSlots = (slots) => {
  if (!slots || typeof slots !== 'object') return {};
  const out = {};
  for (const [k, v] of Object.entries(slots)) {
    const lvl = parseInt(k);
    if (!Number.isFinite(lvl) || lvl < 1 || lvl > 9) continue;
    out[lvl] = { total: v?.total ?? 0, used: v?.used ?? 0 };
  }
  return out;
};

const encodeDictKeys = (obj) => {
  if (!obj || typeof obj !== 'object') return obj;
  const out = {};
  for (const [k, v] of Object.entries(obj)) {
    out[toPascalNoSep(k)] = v;
  }
  return out;
};

const decodeDictKeys = (obj) => {
  if (!obj || typeof obj !== 'object') return obj;
  const out = {};
  for (const [k, v] of Object.entries(obj)) {
    out[toKebabLower(k)] = v;
  }
  return out;
};

const STRIP_FIELDS = ['rowVersion', 'characterId', 'roomId', 'ownerUserId',
  'createdAt', 'updatedAt', 'isArchived', 'id', 'participantId', 'userId',
  'role', 'class', 'attacks', 'spells', 'notes', 'characterName',
  // response-only / auto-computed
  'modifiers', 'proficiencyBonus', 'passivePerception', 'spellSaveDc',
  'spellAttackBonus', 'hitDiceTotal', 'saves'];

const encodeOutgoing = (data) => {
  if (!data || typeof data !== 'object') return data;
  let out = { ...data };
  if (out.skills) {
    out.skillProficiencies = proficienciesFromLevelMap(out.skills);
    delete out.skills;
  }
  if (out.savingThrows) {
    out.saveProficiencies = proficienciesFromLevelMap(out.savingThrows);
    delete out.savingThrows;
  }
  if ('spellSlots' in out) {
    const enc = encodeSpellSlots(out.spellSlots);
    if (enc) out.spellSlots = enc; else delete out.spellSlots;
  }
  out = renameKeys(out, FIELD_MAP_OUT);
  for (const f of STRIP_FIELDS) {
    if (f in out) delete out[f];
  }
  for (const f of ENUM_FIELDS) {
    if (f in out) out[f] = toPascalNoSep(out[f]);
  }
  if (out.skillProficiencies) out.skillProficiencies = encodeDictKeys(out.skillProficiencies);
  if (out.saveProficiencies) out.saveProficiencies = encodeDictKeys(out.saveProficiencies);
  return out;
};

const decodeIncoming = (data) => {
  if (!data || typeof data !== 'object') return data;
  let out = { ...data };
  out = renameKeys(out, FIELD_MAP_IN);
  for (const f of ENUM_FIELDS) {
    if (f in out) out[f] = toKebabLower(out[f]);
  }
  if (out.skills) {
    const sample = Object.values(out.skills)[0];
    if (sample && typeof sample === 'object') {
      out.skills = viewMapToLevelMap(out.skills);
    }
  }
  if (out.saves) {
    const sample = Object.values(out.saves)[0];
    if (sample && typeof sample === 'object') {
      out.savingThrows = viewMapToLevelMap(out.saves);
    } else {
      out.savingThrows = proficienciesToLevelMap(decodeDictKeys(out.saves));
    }
    delete out.saves;
  }
  if (out.skillProficiencies) {
    out.skillProficiencies = decodeDictKeys(out.skillProficiencies);
    if (!out.skills || typeof Object.values(out.skills)[0] === 'object') {
      out.skills = proficienciesToLevelMap(out.skillProficiencies);
    }
  }
  if (out.saveProficiencies) {
    out.saveProficiencies = decodeDictKeys(out.saveProficiencies);
    if (!out.savingThrows) {
      out.savingThrows = proficienciesToLevelMap(out.saveProficiencies);
    }
  }
  if (out.spellSlots) {
    out.spellSlots = decodeSpellSlots(out.spellSlots);
  }
  return out;
};

const decodeResponse = (resp) => {
  const data = resp?.data;
  if (data?.characters && Array.isArray(data.characters)) {
    return { ...resp, data: { ...data, characters: data.characters.map(decodeIncoming) } };
  }
  if (data && typeof data === 'object') {
    return { ...resp, data: decodeIncoming(data) };
  }
  return resp;
};

export const listRoomCharacters = (roomId) =>
  client.get(`/rooms/${roomId}/characters`).then(decodeResponse);
export const createRoomCharacter = (roomId, data) =>
  client.post(`/rooms/${roomId}/characters`, encodeOutgoing(data)).then(decodeResponse);
export const getRoomCharacter = (roomId, characterId) =>
  client.get(`/rooms/${roomId}/characters/${characterId}`).then(decodeResponse);
export const updateRoomCharacter = (roomId, characterId, data) =>
  client.patch(`/rooms/${roomId}/characters/${characterId}`, encodeOutgoing(data)).then(decodeResponse);
export const deleteRoomCharacter = (roomId, characterId) =>
  client.delete(`/rooms/${roomId}/characters/${characterId}`);

export const addAttack = (roomId, characterId, attack) =>
  client.post(`/rooms/${roomId}/characters/${characterId}/attacks`, attack);
export const updateAttackApi = (roomId, characterId, attackId, attack) =>
  client.put(`/rooms/${roomId}/characters/${characterId}/attacks/${attackId}`, attack);
export const deleteAttackApi = (roomId, characterId, attackId) =>
  client.delete(`/rooms/${roomId}/characters/${characterId}/attacks/${attackId}`);

export const addSpell = (roomId, characterId, spell) =>
  client.post(`/rooms/${roomId}/characters/${characterId}/spells`, spell);
export const updateSpellApi = (roomId, characterId, spellId, spell) =>
  client.put(`/rooms/${roomId}/characters/${characterId}/spells/${spellId}`, spell);
export const deleteSpellApi = (roomId, characterId, spellId) =>
  client.delete(`/rooms/${roomId}/characters/${characterId}/spells/${spellId}`);

export { decodeIncoming };

export const __test = { toPascalNoSep, toKebabLower, encodeOutgoing, decodeIncoming };
