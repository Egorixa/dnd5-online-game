import client from './client';

export const createRoom = ({ name, accessMode = 'PRIVATE' }) =>
  client.post('/rooms', { name, accessMode });

export const getMyRooms = ({ status, limit = 50, offset = 0 } = {}) =>
  client.get('/rooms/mine', { params: { status, limit, offset } });

export const leaveRoom = (roomId) =>
  client.post(`/rooms/${roomId}/leave`);

export const kickParticipant = (roomId, { targetUserId, targetParticipantId }) =>
  client.post(`/rooms/${roomId}/kick`, { targetUserId, targetParticipantId });

export const finishRoom = (roomId, { winners = [], losers = [] } = {}) =>
  client.post(`/rooms/${roomId}/finish`, { winners, losers });

export const getRoomEvents = (roomId, limit = 50, offset = 0) =>
  client.get(`/rooms/${roomId}/events`, { params: { limit, offset } });

export const getRoomState = (roomId) =>
  client.get(`/rooms/${roomId}/state`);

export const rollDice = (roomId, { dice, mode = 'PUBLIC', modifier = null }) =>
  client.post(`/rooms/${roomId}/dice/roll`, { dice, mode, modifier });
