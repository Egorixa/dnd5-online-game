import client from './client';

export const listTemplates = () => client.get('/characters');
export const createTemplate = (data) => client.post('/characters', data);
export const getTemplate = (id) => client.get(`/characters/${id}`);
export const updateTemplate = (id, data) => client.patch(`/characters/${id}`, data);
export const deleteTemplate = (id) => client.delete(`/characters/${id}`);

export const listRoomCharacters = (roomId) =>
  client.get(`/rooms/${roomId}/characters`);
export const createRoomCharacter = (roomId, data) =>
  client.post(`/rooms/${roomId}/characters`, data);
export const getRoomCharacter = (roomId, characterId) =>
  client.get(`/rooms/${roomId}/characters/${characterId}`);
export const updateRoomCharacter = (roomId, characterId, data) =>
  client.patch(`/rooms/${roomId}/characters/${characterId}`, data);
export const deleteRoomCharacter = (roomId, characterId) =>
  client.delete(`/rooms/${roomId}/characters/${characterId}`);
