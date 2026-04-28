import client from './client';

export const loginRequest = (username, password) =>
  client.post('/auth/login', { username, password });

export const registerRequest = (username, password, confirmPassword) =>
  client.post('/auth/register', { username, password, confirmPassword });

export const logoutRequest = () =>
  client.post('/auth/logout');

export const getProfile = () =>
  client.get('/auth/profile');

export const updateTheme = (theme) =>
  client.put('/auth/theme', { theme });
