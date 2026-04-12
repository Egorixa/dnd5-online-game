// API-запросы авторизации: login, register, profile, theme, master-games.
import client from './client';

export const loginRequest = (login, password) =>
  client.post('/auth/login', { login, password });

export const registerRequest = (login, password, confirmPassword) =>
  client.post('/auth/register', { login, password, confirmPassword });

export const getProfile = () =>
  client.get('/auth/profile');

export const incrementMasterGames = () =>
  client.post('/auth/master-games/increment');

export const updateTheme = (theme) =>
  client.put('/auth/theme', { theme });
