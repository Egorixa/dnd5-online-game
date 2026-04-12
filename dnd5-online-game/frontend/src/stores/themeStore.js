// Zustand-стор темы: light/dark с persist в localStorage.
import { create } from 'zustand';
import { updateTheme as updateThemeRequest } from '../api/auth';

const useThemeStore = create((set, get) => ({
  theme: localStorage.getItem('theme') || 'light',

  setTheme: (next, { persistRemote = true } = {}) => {
    localStorage.setItem('theme', next);
    set({ theme: next });
    if (persistRemote && localStorage.getItem('token')) {
      updateThemeRequest(next).catch(() => {});
    }
  },

  toggleTheme: () => {
    const next = get().theme === 'light' ? 'dark' : 'light';
    get().setTheme(next);
  },
}));

export default useThemeStore;
