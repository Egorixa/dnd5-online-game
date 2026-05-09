import { create } from 'zustand';
import * as roomsApi from '../api/rooms';

const saveRoom = (room) => {
  if (room) sessionStorage.setItem('dnd_room', JSON.stringify(room));
  else sessionStorage.removeItem('dnd_room');
};

const loadRoom = () => {
  try {
    return JSON.parse(sessionStorage.getItem('dnd_room'));
  } catch {
    return null;
  }
};

const normalizeRoom = (raw, prev = null) => {
  if (!raw) return prev;
  const roomId = raw.roomId ?? raw.RoomId ?? raw.id ?? prev?.roomId;
  return {
    roomId,
    roomCode: raw.roomCode ?? raw.RoomCode ?? raw.code ?? prev?.roomCode,
    name: raw.name ?? raw.Name ?? prev?.name,
    accessMode: raw.accessMode ?? raw.AccessMode ?? prev?.accessMode ?? 'PRIVATE',
    masterId: raw.masterId ?? raw.MasterId ?? prev?.masterId ?? null,
    status: raw.status ?? raw.Status ?? prev?.status ?? 'ACTIVE',
    createdAt: raw.createdAt ?? raw.CreatedAt ?? prev?.createdAt ?? null,
  };
};

const useRoomStore = create((set, get) => ({
  currentRoom: loadRoom(),
  participants: [],
  selectedParticipantId: null,
  eventLog: [],
  loading: false,
  error: null,

  createRoom: async ({ name, accessMode = 'PRIVATE' }) => {
    set({ loading: true, error: null });
    try {
      const { data } = await roomsApi.createRoom({ name, accessMode });
      const room = normalizeRoom(data, get().currentRoom);
      saveRoom(room);
      set({ currentRoom: room, loading: false });
      return room;
    } catch (err) {
      set({ loading: false, error: err?.response?.data?.message || err.message });
      throw err;
    }
  },

  fetchRoomState: async (roomId) => {
    set({ loading: true, error: null });
    const prev = get().currentRoom?.roomId === roomId
      ? get().currentRoom
      : (loadRoom()?.roomId === roomId ? loadRoom() : null);
    try {
      const { data } = await roomsApi.getRoomState(roomId);
      const room = normalizeRoom(data, prev);
      saveRoom(room);
      set({
        currentRoom: room,
        participants: data.participants ?? [],
        loading: false,
      });
      return room;
    } catch (err) {
      const status = err?.response?.status;
      console.warn('[roomStore] fetchRoomState failed:', status, err?.message);
      if (status === 404 || status === 403) {
        saveRoom(null);
        try {
          const map = JSON.parse(localStorage.getItem('dnd_room_names') || '{}');
          delete map[roomId];
          localStorage.setItem('dnd_room_names', JSON.stringify(map));
        } catch { /* ignore */ }
        set({
          currentRoom: null,
          participants: [],
          loading: false,
          error: status === 404
            ? 'Комната не найдена. Возможно, она была завершена или сервер был перезапущен.'
            : 'Доступ к комнате запрещён.',
        });
      } else if (prev) {
        set({ currentRoom: prev, loading: false });
      } else {
        set({ loading: false, error: err.message });
      }
    }
  },

  selectParticipant: (participantId) => set({ selectedParticipantId: participantId }),

  setParticipants: (participants) => set({ participants }),

  addParticipant: (participant) => {
    set({ participants: [...get().participants, participant] });
  },

  removeParticipant: (participantId) => {
    set({
      participants: get().participants.filter((p) => p.participantId !== participantId),
      selectedParticipantId:
        get().selectedParticipantId === participantId ? null : get().selectedParticipantId,
    });
  },

  kickParticipant: async (participantId, userId) => {
    const room = get().currentRoom;
    if (!room) {
      get().removeParticipant(participantId);
      return;
    }
    try {
      await roomsApi.kickParticipant(room.roomId, {
        targetParticipantId: participantId,
        targetUserId: userId,
      });
    } catch (err) {
      console.warn('[roomStore] kickParticipant failed:', err?.message);
    }
  },

  addEvent: (event) => {
    const log = get().eventLog;
    set({ eventLog: [...log, { ...event, timestamp: new Date().toISOString() }] });
  },

  finishRoom: async ({ winners = [], losers = [] } = {}) => {
    const room = get().currentRoom;
    if (room) {
      try {
        await roomsApi.finishRoom(room.roomId, { winners, losers });
      } catch (err) {
        console.warn('[roomStore] finishRoom failed:', err?.message);
      }
    }
    saveRoom(null);
    set({
      currentRoom: null,
      participants: [],
      selectedParticipantId: null,
      eventLog: [],
    });
  },

  leaveRoom: async () => {
    const room = get().currentRoom;
    if (room) {
      try {
        await roomsApi.leaveRoom(room.roomId);
      } catch (err) {
        console.warn('[roomStore] leaveRoom failed:', err?.message);
      }
    }
    saveRoom(null);
    set({
      currentRoom: null,
      participants: [],
      selectedParticipantId: null,
      eventLog: [],
    });
  },

  clearError: () => set({ error: null }),
}));

export default useRoomStore;
