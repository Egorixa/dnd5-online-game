import * as signalR from '@microsoft/signalr';

const HUB_BASE =
  (process.env.REACT_APP_API_URL || 'http://localhost:5050').replace(/\/$/, '');

const HUB_PATH = process.env.REACT_APP_HUB_PATH || '/hubs/room';
const HUB_URL = HUB_BASE + HUB_PATH;

export const SESSION_EVENTS = {
  ROOM_UPDATED: 'room.updated',
  CHARACTER_UPDATED: 'character.updated',
  DICE_ROLLED: 'dice.rolled',
  PARTICIPANT_JOINED: 'participant.joined',
  PARTICIPANT_LEFT: 'participant.left',
};

const HUB_METHODS = {
  JOIN_ROOM: 'JoinRoom',
  LEAVE_ROOM: 'LeaveRoom',
};

export const connectSession = async (roomId) => {
  const connection = new signalR.HubConnectionBuilder()
    .withUrl(HUB_URL, {
      accessTokenFactory: () => localStorage.getItem('token') || '',
    })
    .withAutomaticReconnect([0, 2000, 5000, 10000, 20000])
    .configureLogging(signalR.LogLevel.Warning)
    .build();

  try {
    await connection.start();
    if (roomId) {
      await connection.invoke(HUB_METHODS.JOIN_ROOM, roomId);
    }
    return connection;
  } catch (err) {
    console.warn('[signalr] connect failed:', err?.message);
    return null;
  }
};

export const onSessionEvent = (connection, eventName, handler) => {
  if (!connection) return () => {};
  connection.on(eventName, handler);
  return () => connection.off(eventName, handler);
};

export const disconnectSession = async (connection, roomId) => {
  if (!connection) return;
  try {
    if (roomId) await connection.invoke(HUB_METHODS.LEAVE_ROOM, roomId);
  } catch {

  }
  try {
    await connection.stop();
  } catch {

  }
};
