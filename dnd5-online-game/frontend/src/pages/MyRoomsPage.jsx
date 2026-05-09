import React, { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Users, RefreshCw, LogIn, Clock, Crown } from 'lucide-react';
import * as roomsApi from '../api/rooms';

const STATUS_LABEL = {
  ACTIVE: 'Активна',
  FINISHED: 'Завершена',
};

const STATUS_CLASS = {
  ACTIVE: 'room-card-status-active',
  FINISHED: 'room-card-status-finished',
};

const STATUS_ORDER = { ACTIVE: 0, FINISHED: 1 };

const FILTERS = [
  { value: 'ALL', label: 'Все' },
  { value: 'ACTIVE', label: 'Активные' },
  { value: 'FINISHED', label: 'Завершённые' },
];

const formatRelative = (iso) => {
  if (!iso) return '—';
  const diff = (Date.now() - new Date(iso).getTime()) / 1000;
  if (!Number.isFinite(diff) || diff < 0) return '—';
  if (diff < 60) return 'только что';
  if (diff < 3600) return `${Math.floor(diff / 60)} мин назад`;
  if (diff < 86400) return `${Math.floor(diff / 3600)} ч назад`;
  return `${Math.floor(diff / 86400)} дн назад`;
};

const MyRoomsPage = () => {
  const navigate = useNavigate();
  const [rooms, setRooms] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filter, setFilter] = useState('ALL');

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const params = filter === 'ALL' ? {} : { status: filter };
      const { data } = await roomsApi.getMyRooms(params);
      const list = (data.rooms || []).map((r) => ({
        roomId: r.roomId,
        roomCode: r.roomCode,
        name: r.name,
        accessMode: r.accessMode,
        status: (r.status || 'ACTIVE').toUpperCase(),
        playersCount: r.playersCount ?? 0,
        createdAt: r.createdAt,
      }));
      list.sort((a, b) => {
        const da = STATUS_ORDER[a.status] ?? 3;
        const db = STATUS_ORDER[b.status] ?? 3;
        if (da !== db) return da - db;
        return new Date(b.createdAt || 0) - new Date(a.createdAt || 0);
      });
      setRooms(list);
    } catch (err) {
      setError(err?.response?.data?.message || err.message || 'Не удалось загрузить список комнат');
    } finally {
      setLoading(false);
    }
  }, [filter]);

  useEffect(() => { load(); }, [load]);

  return (
    <div className="rooms-list-page">
      <div className="rooms-list-header">
        <div>
          <h1 className="page-title">Мои комнаты</h1>
          <p className="rooms-list-subtitle">
            Сессии, созданные вами. Откройте активную, чтобы вернуться к управлению.
          </p>
        </div>
        <button
          className="btn-secondary rooms-refresh-btn"
          onClick={load}
          disabled={loading}
        >
          <RefreshCw size={16} className={loading ? 'spin' : ''} />
          {loading ? 'Обновление…' : 'Обновить'}
        </button>
      </div>

      <div className="rooms-list-filters" style={{ display: 'flex', gap: 8, marginBottom: 12, flexWrap: 'wrap' }}>
        {FILTERS.map((f) => (
          <button
            key={f.value}
            className={`btn-secondary ${filter === f.value ? 'active' : ''}`}
            onClick={() => setFilter(f.value)}
            disabled={loading}
          >
            {f.label}
          </button>
        ))}
      </div>

      {error && <div className="server-error rooms-list-error">{error}</div>}

      {loading && rooms.length === 0 ? (
        <div className="rooms-list-empty">Загрузка…</div>
      ) : rooms.length === 0 ? (
        <div className="rooms-list-empty">
          {filter === 'ALL'
            ? 'У вас пока нет созданных комнат. Перейдите на главную и создайте первую сессию.'
            : 'Нет комнат с выбранным статусом.'}
        </div>
      ) : (
        <ul className="rooms-grid">
          {rooms.map((room) => {
            const statusLabel = STATUS_LABEL[room.status] || 'Активна';
            const statusClass = STATUS_CLASS[room.status] || 'room-card-status-active';
            const isFinished = room.status === 'FINISHED';
            return (
              <li key={room.roomId} className="room-card">
                <div className="room-card-header">
                  <code className="room-card-code">{room.roomCode || '—'}</code>
                  <span className={`room-card-status ${statusClass}`}>{statusLabel}</span>
                </div>
                {room.name && (
                  <div className="character-card-name" style={{ fontSize: '1.05rem' }}>
                    {room.name}
                  </div>
                )}
                <div className="room-card-meta">
                  <div className="room-card-meta-row">
                    <Crown size={14} />
                    <span>{room.accessMode === 'PUBLIC' ? 'Публичная' : 'Приватная'}</span>
                  </div>
                  {!isFinished && (
                    <div className="room-card-meta-row">
                      <Users size={14} />
                      <span>Игроков: {room.playersCount}</span>
                    </div>
                  )}
                  <div className="room-card-meta-row">
                    <Clock size={14} />
                    <span>{formatRelative(room.createdAt)}</span>
                  </div>
                </div>
                <button
                  className="btn-main room-card-join"
                  onClick={() => navigate(`/session/${room.roomId}`)}
                  disabled={isFinished}
                >
                  <LogIn size={16} />
                  {isFinished ? 'Завершена' : 'Открыть'}
                </button>
              </li>
            );
          })}
        </ul>
      )}
    </div>
  );
};

export default MyRoomsPage;
