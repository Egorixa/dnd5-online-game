import client from './client';

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
