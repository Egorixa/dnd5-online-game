import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import useRoomStore from '../stores/roomStore';

const HomePage = () => {
  const navigate = useNavigate();
  const createRoom = useRoomStore((s) => s.createRoom);
  const joinRoomByCode = useRoomStore((s) => s.joinRoomByCode);
  const [name, setName] = useState('');
  const [accessMode, setAccessMode] = useState('PRIVATE');
  const [joinCode, setJoinCode] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const handleCreateRoom = async () => {
    if (!name.trim()) {
      setError('Введите название комнаты');
      return;
    }
    setError('');
    setBusy(true);
    try {
      const room = await createRoom(accessMode);
      try {
        const map = JSON.parse(localStorage.getItem('dnd_room_names') || '{}');
        map[room.roomId] = name.trim();
        localStorage.setItem('dnd_room_names', JSON.stringify(map));
      } catch { /* ignore */ }
      navigate(`/session/${room.roomId}`);
    } catch (err) {
      setError(err.response?.data?.message || 'Ошибка создания комнаты');
    } finally {
      setBusy(false);
    }
  };

  const handleJoinByCode = async () => {
    if (!joinCode.trim()) {
      setError('Введите код комнаты');
      return;
    }
    setError('');
    setBusy(true);
    try {
      const room = await joinRoomByCode(joinCode.trim().toUpperCase());
      navigate(`/session/${room.roomId}`);
    } catch (err) {
      setError(err.response?.data?.message || 'Не удалось присоединиться');
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="hero-section">
      <h1 className="hero-title">Добро пожаловать в DnD5 Master</h1>
      <p className="hero-subtitle">
        Создайте комнату и соберите команду для великой игры.
      </p>

      <div className="config-card">
        <div className="input-group">
          <label className="input-label">Название комнаты</label>
          <input
            className={`form-input ${error && !name.trim() ? 'input-error' : ''}`}
            type="text"
            placeholder="Введите название комнаты"
            value={name}
            maxLength={60}
            onChange={(e) => { setName(e.target.value); if (error) setError(''); }}
          />
        </div>

        <div className="input-group">
          <label className="input-label">Доступ к комнате</label>
          <select
            className="form-select"
            value={accessMode}
            onChange={(e) => setAccessMode(e.target.value)}
          >
            <option value="PUBLIC">Публичная (видна в поиске)</option>
            <option value="PRIVATE">Приватная (только по коду)</option>
          </select>
        </div>

        {error && <span className="error-text">{error}</span>}

        <button className="btn-main" onClick={handleCreateRoom} disabled={busy}>
          {busy ? 'Подождите…' : 'Создать новую комнату'}
        </button>

        <div className="config-divider">или</div>

        <div className="input-group">
          <label className="input-label">Присоединиться по коду</label>
          <input
            className="form-input"
            type="text"
            placeholder="ABC123"
            value={joinCode}
            onChange={(e) => setJoinCode(e.target.value)}
          />
        </div>
        <button className="btn-secondary" onClick={handleJoinByCode} disabled={busy}>
          Войти в комнату
        </button>
      </div>
    </div>
  );
};

export default HomePage;
