import React, { useEffect, useState } from 'react';
import { Plus, User, Trash2, AlertTriangle, Pencil } from 'lucide-react';
import * as charactersApi from '../api/characters';
import { RACES, CLASSES, BACKGROUNDS, ALIGNMENTS } from '../constants/dnd';
import {
  toApiRace,
  toApiClass,
  toApiBackground,
  toApiAlignment,
  fromApiRace,
  fromApiClass,
  fromApiBackground,
  fromApiAlignment,
} from '../constants/enumMaps';
import { parseApiError } from '../utils/apiError';
import { TZ_MESSAGES } from '../utils/errorMessages';
import useToastStore from '../stores/toastStore';
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

const buildEditDraft = (t) => ({
  name: t.name || '',
  race: t.race ? fromApiRace(t.race) : '',
  characterClass: (t.characterClass || t.class) ? fromApiClass(t.characterClass || t.class) : '',
  level: t.level ?? 1,
  background: t.background ? fromApiBackground(t.background) : '',
  alignment: t.alignment ? fromApiAlignment(t.alignment) : '',
  currentHp: t.currentHp ?? 0,
  maxHp: t.maxHp ?? 0,
  experiencePoints: t.experiencePoints ?? 0,
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
  const [editTarget, setEditTarget] = useState(null);
  const [editDraft, setEditDraft] = useState(null);
  const [editError, setEditError] = useState('');

  const handleNetwork = (err, fallback) => {
    const parsed = parseApiError(err, fallback);
    const status = err?.response?.status;
    if (!err?.response) {
      useToastStore.getState().error(TZ_MESSAGES.NETWORK_OFFLINE);
      return TZ_MESSAGES.NETWORK_OFFLINE;
    }
    if (status >= 500) {
      useToastStore.getState().error(TZ_MESSAGES.SERVER_ERROR);
      return TZ_MESSAGES.SERVER_ERROR;
    }
    return parsed.message;
  };

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
        setError(handleNetwork(err, 'Не удалось загрузить шаблоны'));
      }
      setTemplates([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const handleCreate = async () => {
    if (!draft.name.trim()) {
      setError(TZ_MESSAGES.FIELD_REQUIRED);
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
        setError(handleNetwork(err, 'Не удалось создать шаблон'));
      }
    } finally {
      setSaving(false);
    }
  };

  const openEdit = (t) => {
    setEditTarget(t);
    setEditDraft(buildEditDraft(t));
    setEditError('');
  };

  const closeEdit = () => {
    setEditTarget(null);
    setEditDraft(null);
    setEditError('');
  };

  const handleSaveEdit = async () => {
    if (!editTarget || !editDraft) return;
    if (!editDraft.name.trim()) {
      setEditError(TZ_MESSAGES.FIELD_REQUIRED);
      return;
    }
    if (editDraft.name.length > 50) {
      setEditError(TZ_MESSAGES.FIELD_TOO_LONG);
      return;
    }
    setSaving(true);
    setEditError('');
    const apiClass = toApiClass(editDraft.characterClass);
    const payload = {
      name: editDraft.name.trim(),
      race: toApiRace(editDraft.race),
      characterClass: apiClass,
      class: apiClass,
      level: Number(editDraft.level) || 1,
      background: editDraft.background ? toApiBackground(editDraft.background) : null,
      alignment: editDraft.alignment ? toApiAlignment(editDraft.alignment) : null,
      currentHp: Number(editDraft.currentHp) || 0,
      maxHp: Number(editDraft.maxHp) || 0,
      experiencePoints: Number(editDraft.experiencePoints) || 0,
    };
    try {
      await charactersApi.updateTemplate(editTarget.characterId || editTarget.id, payload);
      useToastStore.getState().success('Шаблон обновлён');
      closeEdit();
      await load();
    } catch (err) {
      setEditError(handleNetwork(err, 'Не удалось сохранить'));
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    if (!deleteTarget) return;
    try {
      await charactersApi.deleteTemplate(deleteTarget.characterId || deleteTarget.id);
    } catch (err) {
      handleNetwork(err, 'Не удалось удалить');
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
              <li
                key={t.characterId || t.id}
                className="room-card character-card character-card-clickable"
                onClick={() => openEdit(t)}
                role="button"
                tabIndex={0}
                onKeyDown={(e) => { if (e.key === 'Enter') openEdit(t); }}
              >
                <div className="room-card-header">
                  <div className="character-card-avatar"><User size={20} /></div>
                  <div style={{ display: 'flex', gap: 6 }}>
                    <button
                      className="character-card-delete"
                      title="Редактировать"
                      onClick={(e) => { e.stopPropagation(); openEdit(t); }}
                    >
                      <Pencil size={16} />
                    </button>
                    <button
                      className="character-card-delete"
                      title="Удалить шаблон"
                      onClick={(e) => { e.stopPropagation(); setDeleteTarget(t); }}
                    >
                      <Trash2 size={16} />
                    </button>
                  </div>
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
        isOpen={!!editTarget}
        onClose={closeEdit}
        title={`Лист персонажа: ${editTarget?.name || ''}`}
      >
        {editDraft && (
          <div className="character-create-form">
            <div className="input-group">
              <label className="input-label">Имя персонажа</label>
              <input
                className="form-input"
                value={editDraft.name}
                onChange={(e) => setEditDraft({ ...editDraft, name: e.target.value })}
                maxLength={50}
              />
            </div>
            <div className="input-group">
              <label className="input-label">Раса</label>
              <select
                className="form-select"
                value={editDraft.race}
                onChange={(e) => setEditDraft({ ...editDraft, race: e.target.value })}
              >
                <option value="">—</option>
                {RACES.map((r) => <option key={r} value={r}>{r}</option>)}
              </select>
            </div>
            <div className="input-group">
              <label className="input-label">Класс</label>
              <select
                className="form-select"
                value={editDraft.characterClass}
                onChange={(e) => setEditDraft({ ...editDraft, characterClass: e.target.value })}
              >
                <option value="">—</option>
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
                value={editDraft.level}
                onChange={(e) => setEditDraft({ ...editDraft, level: e.target.value })}
              />
            </div>
            <div className="input-group">
              <label className="input-label">Предыстория</label>
              <select
                className="form-select"
                value={editDraft.background}
                onChange={(e) => setEditDraft({ ...editDraft, background: e.target.value })}
              >
                <option value="">—</option>
                {BACKGROUNDS.map((b) => <option key={b} value={b}>{b}</option>)}
              </select>
            </div>
            <div className="input-group">
              <label className="input-label">Мировоззрение</label>
              <select
                className="form-select"
                value={editDraft.alignment}
                onChange={(e) => setEditDraft({ ...editDraft, alignment: e.target.value })}
              >
                <option value="">—</option>
                {ALIGNMENTS.map((a) => <option key={a} value={a}>{a}</option>)}
              </select>
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
              <div className="input-group">
                <label className="input-label">Текущие хиты</label>
                <input
                  className="form-input"
                  type="number"
                  min="0"
                  value={editDraft.currentHp}
                  onChange={(e) => setEditDraft({ ...editDraft, currentHp: e.target.value })}
                />
              </div>
              <div className="input-group">
                <label className="input-label">Максимум хитов</label>
                <input
                  className="form-input"
                  type="number"
                  min="0"
                  value={editDraft.maxHp}
                  onChange={(e) => setEditDraft({ ...editDraft, maxHp: e.target.value })}
                />
              </div>
            </div>
            <div className="input-group">
              <label className="input-label">Очки опыта</label>
              <input
                className="form-input"
                type="number"
                min="0"
                max="999999"
                value={editDraft.experiencePoints}
                onChange={(e) => setEditDraft({ ...editDraft, experiencePoints: e.target.value })}
              />
            </div>
            {editError && <div className="server-error">{editError}</div>}
            <div className="modal-actions">
              <button className="btn-secondary" onClick={closeEdit} disabled={saving}>
                Отмена
              </button>
              <button className="btn-main" onClick={handleSaveEdit} disabled={saving}>
                {saving ? 'Сохранение…' : 'Сохранить'}
              </button>
            </div>
          </div>
        )}
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
