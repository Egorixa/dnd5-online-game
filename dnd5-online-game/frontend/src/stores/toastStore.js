import { create } from 'zustand';

let nextId = 1;

const useToastStore = create((set, get) => ({
  toasts: [],
  push: (message, type = 'error', duration = 4000) => {
    if (!message) return null;
    const id = nextId++;
    set((s) => ({ toasts: [...s.toasts, { id, message, type, duration }] }));
    return id;
  },
  remove: (id) => set((s) => ({ toasts: s.toasts.filter((t) => t.id !== id) })),
  error: (message, duration) => get().push(message, 'error', duration),
  success: (message, duration) => get().push(message, 'success', duration),
}));

export default useToastStore;
