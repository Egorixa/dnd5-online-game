import React, { useCallback, useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Users, RefreshCw, LogIn, Clock, Crown } from 'lucide-react';
import * as roomsApi from '../api/rooms';
import useRoomStore from '../stores/roomStore';
import { parseApiError } from '../utils/apiError';

const STATUS_LABEL = {
  ACTIVE: 'Активна',
  PAUSED: 'Пауза',
  FINISHED: 'Завершена',
};

const STATUS_CLASS = {
  ACTIVE: 'room-card-status-active',
  PAUSED: 'room-card-status-paused',
  FINISHED: 'room-card-status-finished',
};

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
      setError(parseApiError(err, 'Не удалось загрузить список комнат').message);
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
      const status = err?.response?.status;
      const parsed = parseApiError(err, 'Не удалось присоединиться');
      if (status === 403 || status === 404 || parsed.code === 'ALREADY_FINISHED') {
        setRooms((prev) => prev.filter((r) => r.roomId !== room.roomId));
        setError(`${parsed.message}. Список обновлён.`);
        load(true);
      } else {
        setError(parsed.message);
      }
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
            const statusKey = (room.status || 'ACTIVE').toUpperCase();
            const statusLabel = STATUS_LABEL[statusKey] || 'Активна';
            const statusClass = STATUS_CLASS[statusKey] || 'room-card-status-active';
            const isFinished = statusKey === 'FINISHED';
            return (
            <li key={room.roomId} className="room-card">
              <div className="room-card-header">
                <code className="room-card-code">{room.roomCode}</code>
                <span className={`room-card-status ${statusClass}`}>{statusLabel}</span>
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
                disabled={joiningCode === room.roomCode || isFinished}
              >
                <LogIn size={16} />
                {isFinished
                  ? 'Завершена'
                  : joiningCode === room.roomCode
                    ? 'Подключение…'
                    : 'Присоединиться'}
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
