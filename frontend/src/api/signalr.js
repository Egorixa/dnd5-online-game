// SignalR-клиент для реал-тайм сессии (/hubs/session).
import * as signalR from '@microsoft/signalr';

const HUB_URL =
    (process.env.REACT_APP_API_URL || 'http://localhost:5001/api').replace(
        /\/api\/?$/,
        '',
    ) + '/hubs/session';

export const SESSION_EVENTS = {
    PLAYER_JOINED: 'PlayerJoined',
    PLAYER_LEFT: 'PlayerLeft',
    PLAYER_KICKED: 'PlayerKicked',
    CHARACTER_UPDATED: 'CharacterUpdated',
    DICE_ROLLED: 'DiceRolled',
    CHAT_MESSAGE: 'ChatMessage',
    SESSION_ENDED: 'SessionEnded',
};

export const SESSION_ACTIONS = {
    JOIN_ROOM: 'JoinRoom',
    LEAVE_ROOM: 'LeaveRoom',
    KICK_PLAYER: 'KickPlayer',
    UPDATE_CHARACTER: 'UpdateCharacter',
    ROLL_DICE: 'RollDice',
    SEND_CHAT: 'SendChat',
    END_SESSION: 'EndSession',
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
            await connection.invoke(SESSION_ACTIONS.JOIN_ROOM, roomId);
        }
        return connection;
    } catch (err) {
        console.warn(
            '[signalr] connect failed — running in offline mode:',
            err?.message,
        );
        return null;
    }
};

export const onSessionEvent = (connection, eventName, handler) => {
    if (!connection) return () => {};
    connection.on(eventName, handler);
    return () => connection.off(eventName, handler);
};

export const sendSessionAction = async (connection, action, ...args) => {
    if (!connection) return;
    try {
        await connection.invoke(action, ...args);
    } catch (err) {
        console.warn(`[signalr] invoke ${action} failed:`, err?.message);
    }
};

export const disconnectSession = async (connection, roomId) => {
    if (!connection) return;
    try {
        if (roomId) await connection.invoke(SESSION_ACTIONS.LEAVE_ROOM, roomId);
    } catch {
        /* ignore */
    }
    try {
        await connection.stop();
    } catch {
        /* ignore */
    }
};
