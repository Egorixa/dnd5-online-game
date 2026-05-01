import React, { useEffect, useState } from 'react';
import { Plus, User, Trash2, AlertTriangle } from 'lucide-react';
import * as charactersApi from '../api/characters';
import { RACES, CLASSES } from '../constants/dnd';
import {
  toApiRace,
  toApiClass,
  fromApiRace,
  fromApiClass,
} from '../constants/enumMaps';
import { parseApiError } from '../utils/apiError';
import Modal from '../components/ui/Modal';

const formatDate = (iso) => {
  if (!iso) return '—';
  try {
    return new Date(iso).toLocaleDateString('ru-RU', {
      day: '2-digit', month: '2-digit', year: 'numeric',
    });
  } catch {
    return '—';
  }
};

const emptyDraft = () => ({
  name: '', race: 'Человек', characterClass: 'Воин', level: 1,
});

const CharactersListPage = () => {
  const [templates, setTemplates] = useState([]);
  const [loading, setLoading] = useState(true);
  const [endpointMissing, setEndpointMissing] = useState(false);
  const [error, setError] = useState('');
  const [showCreate, setShowCreate] = useState(false);
  const [draft, setDraft] = useState(emptyDraft());
  const [saving, setSaving] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState(null);

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const { data } = await charactersApi.listTemplates();
      const list = Array.isArray(data) ? data : (data.characters || data.items || []);
      setTemplates(list);
      setEndpointMissing(false);
    } catch (err) {
      if (err.response?.status === 404) {
        setEndpointMissing(true);
      } else {
        setError(parseApiError(err, 'Не удалось загрузить шаблоны').message);
      }
      setTemplates([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const handleCreate = async () => {
    if (!draft.name.trim()) {
      setError('Имя персонажа обязательно');
      return;
    }
    setSaving(true);
    setError('');
    try {
      const apiClass = toApiClass(draft.characterClass);
      await charactersApi.createTemplate({
        name: draft.name.trim(),
        race: toApiRace(draft.race),
        characterClass: apiClass,
        class: apiClass,
        level: Number(draft.level) || 1,
      });
      setShowCreate(false);
      setDraft(emptyDraft());
      await load();
    } catch (err) {
      if (err.response?.status === 404) {
        setEndpointMissing(true);
        setShowCreate(false);
      } else {
        setError(parseApiError(err, 'Не удалось создать шаблон').message);
      }
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    if (!deleteTarget) return;
    try {
      await charactersApi.deleteTemplate(deleteTarget.characterId || deleteTarget.id);
    } catch (err) {
      console.warn('[characters] delete failed:', err.message);
    }
    setDeleteTarget(null);
    load();
  };

  return (
    <div className="characters-list-page">
      <div className="rooms-list-header">
        <div>
          <h1 className="page-title">Шаблоны персонажей</h1>
          <p className="rooms-list-subtitle">Сохранённые листы для будущих сессий</p>
        </div>
        <button
          className="btn-main rooms-refresh-btn"
          onClick={() => setShowCreate(true)}
          disabled={endpointMissing}
        >
          <Plus size={16} /> Создать шаблон
        </button>
      </div>

      {endpointMissing && (
        <div className="rooms-list-empty">
          <AlertTriangle size={20} style={{ display: 'inline', marginRight: 8, verticalAlign: 'middle' }} />
          Эндпоинты <code>/characters</code> ещё не реализованы на бэкенде.
          В письме бек обещал добавить — нужно дождаться его коммита.
        </div>
      )}

      {error && !endpointMissing && (
        <div className="server-error rooms-list-error">{error}</div>
      )}

      {!endpointMissing && (
        loading ? (
          <div className="rooms-list-empty">Загрузка…</div>
        ) : templates.length === 0 ? (
          <div className="rooms-list-empty">
            У вас пока нет сохранённых шаблонов. Создайте первого героя — он будет
            доступен во всех будущих сессиях.
          </div>
        ) : (
          <ul className="rooms-grid">
            {templates.map((t) => (
              <li key={t.characterId || t.id} className="room-card character-card">
                <div className="room-card-header">
                  <div className="character-card-avatar"><User size={20} /></div>
                  <button
                    className="character-card-delete"
                    title="Удалить шаблон"
                    onClick={() => setDeleteTarget(t)}
                  >
                    <Trash2 size={16} />
                  </button>
                </div>
                <div className="character-card-name">{t.name || 'Без имени'}</div>
                <div className="room-card-meta">
                  <div className="room-card-meta-row">
                    <span>Раса: <strong>{t.race ? fromApiRace(t.race) : '—'}</strong></span>
                  </div>
                  <div className="room-card-meta-row">
                    <span>Класс: <strong>{(t.characterClass || t.class) ? fromApiClass(t.characterClass || t.class) : '—'}</strong></span>
                  </div>
                  <div className="room-card-meta-row">
                    <span>Уровень: <strong>{t.level ?? '—'}</strong></span>
                  </div>
                  <div className="room-card-meta-row">
                    <span>Изменён: {formatDate(t.updatedAt || t.createdAt)}</span>
                  </div>
                </div>
              </li>
            ))}
          </ul>
        )
      )}

      <Modal
        isOpen={showCreate}
        onClose={() => { setShowCreate(false); setError(''); }}
        title="Новый шаблон персонажа"
      >
        <div className="character-create-form">
          <div className="input-group">
            <label className="input-label">Имя персонажа</label>
            <input
              className="form-input"
              value={draft.name}
              onChange={(e) => setDraft({ ...draft, name: e.target.value })}
              maxLength={50}
              placeholder="Торин Дубощит"
            />
          </div>
          <div className="input-group">
            <label className="input-label">Раса</label>
            <select
              className="form-select"
              value={draft.race}
              onChange={(e) => setDraft({ ...draft, race: e.target.value })}
            >
              {RACES.map((r) => <option key={r} value={r}>{r}</option>)}
            </select>
          </div>
          <div className="input-group">
            <label className="input-label">Класс</label>
            <select
              className="form-select"
              value={draft.characterClass}
              onChange={(e) => setDraft({ ...draft, characterClass: e.target.value })}
            >
              {CLASSES.map((c) => <option key={c} value={c}>{c}</option>)}
            </select>
          </div>
          <div className="input-group">
            <label className="input-label">Уровень</label>
            <input
              className="form-input"
              type="number"
              min="1"
              max="20"
              value={draft.level}
              onChange={(e) => setDraft({ ...draft, level: e.target.value })}
            />
          </div>
          {error && <div className="server-error">{error}</div>}
          <div className="modal-actions">
            <button className="btn-secondary" onClick={() => setShowCreate(false)} disabled={saving}>
              Отмена
            </button>
            <button className="btn-main" onClick={handleCreate} disabled={saving}>
              {saving ? 'Создание…' : 'Создать'}
            </button>
          </div>
        </div>
      </Modal>

      <Modal
        isOpen={!!deleteTarget}
        onClose={() => setDeleteTarget(null)}
        title="Удалить шаблон?"
      >
        <p style={{ color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>
          Шаблон <strong>«{deleteTarget?.name}»</strong> будет удалён безвозвратно.
        </p>
        <div className="modal-actions">
          <button className="btn-secondary" onClick={() => setDeleteTarget(null)}>Отмена</button>
          <button className="btn-danger" onClick={handleDelete}>Удалить</button>
        </div>
      </Modal>
    </div>
  );
};

export default CharactersListPage;
