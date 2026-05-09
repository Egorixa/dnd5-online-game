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
  'role', 'class'];

const encodeOutgoing = (data) => {
  if (!data || typeof data !== 'object') return data;
  const out = { ...data };
  for (const f of STRIP_FIELDS) {
    if (f in out) delete out[f];
  }
  for (const f of ENUM_FIELDS) {
    if (f in out) out[f] = toPascalNoSep(out[f]);
  }
  if (Array.isArray(out.attacks)) {
    out.attacks = out.attacks
      .filter((a) => a && (a.name || '').trim() && (a.damage || '').trim())
      .map((a) => ({
        name: String(a.name).trim(),
        attackBonus: Number.isFinite(a.attackBonus) ? a.attackBonus : 0,
        damage: String(a.damage).trim(),
      }));
  }
  if (Array.isArray(out.spells)) {
    out.spells = out.spells
      .filter((s) => s && (s.name || '').trim())
      .map((s) => ({ ...s }));
  }
  if (out.skillProficiencies) out.skillProficiencies = encodeDictKeys(out.skillProficiencies);
  if (out.saveProficiencies) out.saveProficiencies = encodeDictKeys(out.saveProficiencies);
  return out;
};

const decodeIncoming = (data) => {
  if (!data || typeof data !== 'object') return data;
  const out = { ...data };
  for (const f of ENUM_FIELDS) {
    if (f in out) out[f] = toKebabLower(out[f]);
  }
  if (out.skillProficiencies) out.skillProficiencies = decodeDictKeys(out.skillProficiencies);
  if (out.saveProficiencies) out.saveProficiencies = decodeDictKeys(out.saveProficiencies);
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

export { decodeIncoming };

export const __test = { toPascalNoSep, toKebabLower, encodeOutgoing, decodeIncoming };
