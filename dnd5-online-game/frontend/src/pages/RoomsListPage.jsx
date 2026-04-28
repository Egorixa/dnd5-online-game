import React, { useCallback, useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Users, RefreshCw, LogIn, Clock, Crown } from 'lucide-react';
import * as roomsApi from '../api/rooms';
import useRoomStore from '../stores/roomStore';

const REFRESH_INTERVAL_MS = 15000;

const formatRelative = (iso) => {
  if (!iso) return '—';
  const diff = (Date.now() - new Date(iso).getTime()) / 1000;
  if (!Number.isFinite(diff) || diff < 0) return '—';
  if (diff < 60) return 'только что';
  if (diff < 3600) return `${Math.floor(diff / 60)} мин назад`;
  if (diff < 86400) return `${Math.floor(diff / 3600)} ч назад`;
  return `${Math.floor(diff / 86400)} дн назад`;
};

const RoomsListPage = () => {
  const navigate = useNavigate();
  const joinRoomByCode = useRoomStore((s) => s.joinRoomByCode);
  const [rooms, setRooms] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [joiningCode, setJoiningCode] = useState(null);
  const timerRef = useRef(null);

  const load = useCallback(async (silent = false) => {
    if (!silent) setLoading(true);
    setError('');
    try {
      const { data } = await roomsApi.getPublicRooms(50, 0);
      setRooms(data.rooms || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Не удалось загрузить список комнат');
    } finally {
      if (!silent) setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
    timerRef.current = setInterval(() => load(true), REFRESH_INTERVAL_MS);
    return () => clearInterval(timerRef.current);
  }, [load]);

  const handleJoin = async (room) => {
    setJoiningCode(room.roomCode);
    try {
      const joined = await joinRoomByCode(room.roomCode);
      navigate(`/session/${joined.roomId}`);
    } catch (err) {
      setError(err.response?.data?.message || 'Не удалось присоединиться');
    } finally {
      setJoiningCode(null);
    }
  };

  return (
    <div className="rooms-list-page">
      <div className="rooms-list-header">
        <div>
          <h1 className="page-title">Список комнат</h1>
          <p className="rooms-list-subtitle">Активные публичные сессии других мастеров</p>
        </div>
        <button
          className="btn-secondary rooms-refresh-btn"
          onClick={() => load()}
          disabled={loading}
        >
          <RefreshCw size={16} className={loading ? 'spin' : ''} />
          {loading ? 'Обновление…' : 'Обновить'}
        </button>
      </div>

      {error && <div className="server-error rooms-list-error">{error}</div>}

      {loading && rooms.length === 0 ? (
        <div className="rooms-list-empty">Загрузка…</div>
      ) : rooms.length === 0 ? (
        <div className="rooms-list-empty">
          Сейчас нет открытых публичных комнат. Создайте свою — и она появится здесь
          у других мастеров.
        </div>
      ) : (
        <ul className="rooms-grid">
          {rooms.map((room) => {
            let localName = null;
            try {
              const map = JSON.parse(localStorage.getItem('dnd_room_names') || '{}');
              localName = map[room.roomId];
            } catch { /* ignore */ }
            const displayName = room.name || localName;
            return (
            <li key={room.roomId} className="room-card">
              <div className="room-card-header">
                <code className="room-card-code">{room.roomCode}</code>
                <span className="room-card-status room-card-status-active">Активна</span>
              </div>
              {displayName && (
                <div className="character-card-name" style={{ fontSize: '1.05rem' }}>
                  {displayName}
                </div>
              )}
              <div className="room-card-meta">
                <div className="room-card-meta-row">
                  <Crown size={14} />
                  <span title={room.masterId}>
                    Мастер: {room.masterId ? `${String(room.masterId).slice(0, 8)}…` : '—'}
                  </span>
                </div>
                <div className="room-card-meta-row">
                  <Users size={14} />
                  <span>Игроков: {room.playersCount ?? 0}</span>
                </div>
                <div className="room-card-meta-row">
                  <Clock size={14} />
                  <span>{formatRelative(room.createdAt)}</span>
                </div>
              </div>
              <button
                className="btn-main room-card-join"
                onClick={() => handleJoin(room)}
                disabled={joiningCode === room.roomCode}
              >
                <LogIn size={16} />
                {joiningCode === room.roomCode ? 'Подключение…' : 'Присоединиться'}
              </button>
            </li>
            );
          })}
        </ul>
      )}
    </div>
  );
};

export default RoomsListPage;
