// Zustand-стор комнат: CRUD через api/rooms.js с graceful fallback на локальный режим.
import { create } from 'zustand';
import * as roomsApi from '../api/rooms';

const generateInviteCode = () =>
  Math.random().toString(36).substring(2, 8).toUpperCase();

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


const normalizeRoom = (raw) => {
  if (!raw) return null;
  return {
    id: raw.id ?? raw.Id ?? raw.roomId ?? raw.RoomId,
    name: raw.name ?? raw.Name,
    accessType: raw.accessType ?? raw.AccessType ?? 'Private',
    inviteCode: raw.inviteCode ?? raw.InviteCode ?? raw.code ?? raw.Code,
    status: raw.status ?? raw.Status ?? 'active',
    createdAt: raw.createdAt ?? raw.CreatedAt ?? new Date().toISOString(),
  };
};


const buildLocalRoom = (name, accessType) => ({
  id: crypto.randomUUID(),
  name: name.trim(),
  accessType: accessType || 'Private',
  inviteCode: generateInviteCode(),
  status: 'active',
  createdAt: new Date().toISOString(),
  __local: true,
});

const useRoomStore = create((set, get) => ({
  currentRoom: loadRoom(),
  players: [],
  selectedPlayerId: null,
  eventLog: [],
  loading: false,
  error: null,

  createRoom: async (name, accessType) => {
    set({ loading: true, error: null });
    try {
      const { data } = await roomsApi.createRoom(name.trim(), accessType || 'Private');
      const room = normalizeRoom(data);
      saveRoom(room);
      set({ currentRoom: room, loading: false });
      return room;
    } catch (err) {
      console.warn('[roomStore] createRoom fell back to local-only:', err?.message);
      const room = buildLocalRoom(name, accessType);
      saveRoom(room);
      set({ currentRoom: room, loading: false });
      return room;
    }
  },

  fetchRoom: async (id) => {
    const cached = get().currentRoom;
    if (cached && cached.id === id) {
      set({ loading: false });
      return;
    }
    const saved = loadRoom();
    if (saved && saved.id === id) {
      set({ currentRoom: saved, loading: false });
      return;
    }
    set({ loading: true, error: null });
    try {
      const { data } = await roomsApi.getRoom(id);
      const room = normalizeRoom(data);
      saveRoom(room);
      set({ currentRoom: room, loading: false });
    } catch (err) {
      console.warn('[roomStore] fetchRoom fell back to local stub:', err?.message);
      const fallback = {
        id,
        name: 'Комната',
        accessType: 'Private',
        inviteCode: generateInviteCode(),
        status: 'active',
        __local: true,
      };
      saveRoom(fallback);
      set({ currentRoom: fallback, loading: false });
    }
  },

  selectPlayer: (playerId) => set({ selectedPlayerId: playerId }),

  addEvent: (event) => {
    const log = get().eventLog;
    set({ eventLog: [...log, { ...event, timestamp: new Date().toISOString() }] });
  },

  setPlayers: (players) => set({ players }),

  addPlayer: (player) => {
    set({ players: [...get().players, player] });
  },

  removePlayer: (playerId) => {
    const room = get().currentRoom;
    if (room && !room.__local) {
      roomsApi.kickPlayer(room.id, playerId).catch((err) => {
        console.warn('[roomStore] kickPlayer failed:', err?.message);
      });
    }
    set({
      players: get().players.filter((p) => p.id !== playerId),
      selectedPlayerId: get().selectedPlayerId === playerId ? null : get().selectedPlayerId,
    });
  },

  updatePlayer: (playerId, data) => {
    set({
      players: get().players.map((p) =>
        p.id === playerId ? { ...p, ...data } : p
      ),
    });
  },

  leaveRoom: () => {
    const room = get().currentRoom;
    if (room && !room.__local) {
      roomsApi.deleteRoom(room.id).catch((err) => {
        console.warn('[roomStore] deleteRoom failed:', err?.message);
      });
    }
    saveRoom(null);
    set({
      currentRoom: null,
      players: [],
      selectedPlayerId: null,
      eventLog: [],
    });
  },

  clearError: () => set({ error: null }),
}));

export default useRoomStore;
