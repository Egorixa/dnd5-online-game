// API-запросы для CRUD комнат и управления игроками.
import client from './client';

export const createRoom = (name, accessType) =>
  client.post('/rooms', { name, accessType });

export const getRooms = () => client.get('/rooms');

export const getRoom = (id) => client.get(`/rooms/${id}`);

export const deleteRoom = (id) => client.delete(`/rooms/${id}`);

export const getRoomPlayers = (roomId) => client.get(`/rooms/${roomId}/players`);

export const kickPlayer = (roomId, playerId) =>
  client.delete(`/rooms/${roomId}/players/${playerId}`);
