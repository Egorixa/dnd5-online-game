// Главная страница: создание комнаты (название + тип доступа).
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import useRoomStore from '../stores/roomStore';

const HomePage = () => {
  const navigate = useNavigate();
  const createRoom = useRoomStore((s) => s.createRoom);
  const [access, setAccess] = useState('Private');
  const [roomName, setRoomName] = useState('');
  const [error, setError] = useState('');
  const [creating, setCreating] = useState(false);

  const handleCreateRoom = async () => {
    if (!roomName.trim()) {
      setError('Введите название комнаты');
      return;
    }
    setError('');
    setCreating(true);
    try {
      const room = await createRoom(roomName.trim(), access);
      navigate(`/session/${room.id}`);
    } catch (err) {
      setError(err.response?.data?.message || 'Ошибка создания комнаты');
    } finally {
      setCreating(false);
    }
  };

  return (
    <div className="hero-section">
      <h1 className="hero-title">Добро пожаловать в сервис для игры в DnD5!</h1>
      <p className="hero-subtitle">
        Создайте комнату и объедините друзей и героев для проведения великой игры.
      </p>

      <div className="config-card">
        <div className="input-group">
          <label className="input-label">Название комнаты</label>
          <input
            className={`form-input ${error ? 'input-error' : ''}`}
            type="text"
            placeholder="Введите название комнаты"
            value={roomName}
            onChange={(e) => {
              setRoomName(e.target.value);
              if (error) setError('');
            }}
          />
          {error && <span className="error-text">{error}</span>}
        </div>

        <div className="input-group">
          <label className="input-label">Доступ к комнате</label>
          <select
            className="form-select"
            value={access}
            onChange={(e) => setAccess(e.target.value)}
          >
            <option value="Public">Публичная (видна в поиске)</option>
            <option value="Private">Приватная (только по коду)</option>
          </select>
        </div>

        <button className="btn-main" onClick={handleCreateRoom} disabled={creating}>
          {creating ? 'Создание...' : 'Создать новую комнату'}
        </button>

        <p style={{ fontSize: '0.9rem', color: 'var(--text-secondary)', marginTop: '1.5rem' }}>
          После нажатия кнопки вы получите уникальный код для приглашения игроков.
        </p>
      </div>
    </div>
  );
};

export default HomePage;
