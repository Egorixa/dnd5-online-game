import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Copy, LogOut, Check, Sun, Moon, RefreshCw } from 'lucide-react';
import useRoomStore from '../stores/roomStore';
import useThemeStore from '../stores/themeStore';
import useAuthStore from '../stores/authStore';
import useToastStore from '../stores/toastStore';
import * as roomsApi from '../api/rooms';
import * as charactersApi from '../api/characters';
import { decodeIncoming as decodeCharacter } from '../api/characters';
import { getProfile } from '../api/auth';
import {
  connectSession, onSessionEvent, disconnectSession, SESSION_EVENTS,
} from '../api/signalr';
import PlayerList from '../components/session/PlayerList';
import DicePanel from '../components/session/DicePanel';
import MagicBall from '../components/session/MagicBall';
import EventLog from '../components/session/EventLog';
import InitiativeTracker from '../components/session/InitiativeTracker';
import EndSessionModal from '../components/session/EndSessionModal';
import CharacterViewer from '../components/session/CharacterViewer';

const DICE_KIND_NAMES = ['d4', 'd6', 'd8', 'd10', 'd12', 'd20', 'd100', 'magic_ball'];
const formatDice = (d) => {
  if (typeof d === 'number') return DICE_KIND_NAMES[d] || `d${d}`;
  if (typeof d === 'string') return d.toLowerCase();
  return '?';
};

const toUiPlayer = (p, characters = {}) => ({
  id: p.participantId,
  participantId: p.participantId,
  userId: p.userId,
  username: p.username || (p.userId ? `Игрок ${String(p.userId).slice(0, 6)}` : 'Игрок'),
  role: p.role,
  characterName: characters[p.userId]?.name || null,
  ...characters[p.userId],
});

const SessionPage = () => {
  const { roomId } = useParams();
  const navigate = useNavigate();

  const {
    currentRoom, participants, selectedParticipantId, eventLog,
    loading, error,
    fetchRoomState, selectParticipant, addEvent,
    addParticipant, removeParticipant,
    kickParticipant, finishRoom,
  } = useRoomStore();

  const signalrRef = useRef(null);
  const [characters, setCharacters] = useState({});
  const [showEndModal, setShowEndModal] = useState(false);
  const [codeCopied, setCodeCopied] = useState(false);

  const { theme, toggleTheme } = useThemeStore();
  const { user, updateUser } = useAuthStore();

  const [leftW, setLeftW] = useState(() => {
    const v = parseInt(localStorage.getItem('session-left-w') || '260', 10);
    return Number.isFinite(v) ? v : 260;
  });
  const [rightW, setRightW] = useState(() => {
    const v = parseInt(localStorage.getItem('session-right-w') || '320', 10);
    return Number.isFinite(v) ? v : 320;
  });

  const startResize = (side) => (e) => {
    e.preventDefault();
    e.stopPropagation();
    const target = e.currentTarget;
    if (target && target.setPointerCapture && e.pointerId != null) {
      try { target.setPointerCapture(e.pointerId); } catch {  }
    }
    const startX = e.clientX;
    const startLeft = leftW;
    const startRight = rightW;
    const onMove = (ev) => {
      const dx = ev.clientX - startX;
      if (side === 'left') {
        const next = Math.min(520, Math.max(200, startLeft + dx));
        setLeftW(next);
        localStorage.setItem('session-left-w', String(next));
      } else {
        const next = Math.min(560, Math.max(240, startRight - dx));
        setRightW(next);
        localStorage.setItem('session-right-w', String(next));
      }
    };
    const onUp = () => {
      window.removeEventListener('pointermove', onMove);
      window.removeEventListener('pointerup', onUp);
      window.removeEventListener('pointercancel', onUp);
      document.body.style.cursor = '';
      document.body.style.userSelect = '';
    };
    document.body.style.cursor = 'col-resize';
    document.body.style.userSelect = 'none';
    window.addEventListener('pointermove', onMove);
    window.addEventListener('pointerup', onUp);
    window.addEventListener('pointercancel', onUp);
  };

  useEffect(() => {
    if (!currentRoom || currentRoom.roomId !== roomId) return;
    const userId = user?.userId ?? user?.id ?? null;
    const masterId = currentRoom.masterId ?? null;
    if (userId && masterId && String(userId) !== String(masterId)) {
      useToastStore.getState().error(
        'Игровая сессия в браузере доступна только Мастеру. Игроки подключаются через мобильное приложение.',
      );
      navigate('/', { replace: true });
    }
  }, [currentRoom, user, roomId, navigate]);

  const reloadCharacters = useCallback(async () => {
    try {
      const { data } = await charactersApi.listRoomCharacters(roomId);
      const map = {};
      (data.characters || []).forEach((c) => {
        if (c.ownerUserId) map[c.ownerUserId] = c;
      });
      setCharacters(map);
    } catch (err) {
      console.warn('[session] reloadCharacters failed:', err?.message);
    }
  }, [roomId]);

  const handleSyncSheets = async () => {
    addEvent({ type: 'system', text: 'Синхронизация листов персонажей…' });
    await Promise.all([fetchRoomState(roomId), reloadCharacters()]);
    addEvent({ type: 'system', text: 'Листы персонажей синхронизированы.' });
  };

  useEffect(() => {
    fetchRoomState(roomId);
    addEvent({ type: 'system', text: 'Сессия открыта. Ожидание игроков…' });
    reloadCharacters();
  }, [roomId, reloadCharacters]);

  useEffect(() => {
    let unsubs = [];
    let cancelled = false;

    connectSession(roomId).then((conn) => {
      if (cancelled || !conn) return;
      signalrRef.current = conn;

      unsubs.push(
        onSessionEvent(conn, SESSION_EVENTS.PARTICIPANT_JOINED, (p) => {
          addParticipant(p);
          const who = p.username
            || (p.userId ? `Игрок ${String(p.userId).slice(0, 6)}` : 'Игрок');
          addEvent({ type: 'join', text: `${who} присоединился` });
          fetchRoomState(roomId);
          reloadCharacters();
        }),
        onSessionEvent(conn, SESSION_EVENTS.PARTICIPANT_LEFT, ({ participantId, username, kicked }) => {
          const who = username || 'Игрок';
          removeParticipant(participantId);
          addEvent({
            type: 'leave',
            text: kicked ? `${who} исключён мастером` : `${who} покинул сессию`,
          });
          fetchRoomState(roomId);
          reloadCharacters();
        }),
        onSessionEvent(conn, SESSION_EVENTS.CHARACTER_UPDATED, (payload) => {
          const action = payload?.action;
          const character = payload?.character ?? (payload?.ownerUserId ? payload : null);
          const ownerUserId = character?.ownerUserId ?? payload?.ownerUserId;
          const charName = payload?.characterName || character?.name;
          const ownerName = payload?.ownerUserName;

          if (action === 'deleted') {
            if (ownerUserId) {
              setCharacters((prev) => {
                const next = { ...prev };
                delete next[ownerUserId];
                return next;
              });
            }
          } else if (character && ownerUserId) {
            setCharacters((prev) => ({ ...prev, [ownerUserId]: decodeCharacter(character) }));
          }

          const who = charName && ownerName
            ? `${charName} (${ownerName})`
            : charName || ownerName || null;
          const verb = action === 'created' ? 'создан'
            : action === 'deleted' ? 'удалён'
            : 'обновлён';
          addEvent({
            type: 'system',
            text: who ? `Лист персонажа ${verb}: ${who}` : `Лист персонажа ${verb}`,
          });
          fetchRoomState(roomId);
          reloadCharacters();
        }),
        onSessionEvent(conn, SESSION_EVENTS.DICE_ROLLED, (roll) => {
          const dice = formatDice(roll.dice);
          const isHidden = roll.mode === 'HIDDEN' || roll.mode === 1;
          const charName = roll.characterName;
          const userName = roll.userName;
          const who = charName && userName
            ? `${charName} (${userName})`
            : charName || userName || 'Игрок';
          if (dice === 'magic_ball' || roll.magicBallAnswer) {
            addEvent({
              type: 'system',
              text: `Магический шар (${who}): «${roll.magicBallAnswer || '—'}»`,
            });
            return;
          }
          const total = roll.total ?? roll.result;
          addEvent({
            type: 'dice',
            text: `${who} бросил ${dice}: ${total}${isHidden ? ' (скрытый)' : ''}`,
          });
        }),
        onSessionEvent(conn, SESSION_EVENTS.ROOM_UPDATED, (state) => {
          if (state?.status === 'FINISHED') {
            addEvent({ type: 'system', text: 'Сессия завершена мастером' });
            setTimeout(() => navigate('/'), 1500);
          }
        }),
      );
    });

    return () => {
      cancelled = true;
      unsubs.forEach((fn) => fn());
      if (signalrRef.current) {
        disconnectSession(signalrRef.current, roomId);
        signalrRef.current = null;
      }
    };
  }, [roomId]);

  const handleCopyCode = () => {
    const code = currentRoom?.roomCode || roomId;
    navigator.clipboard.writeText(code).then(() => {
      setCodeCopied(true);
      setTimeout(() => setCodeCopied(false), 2000);
    });
  };

  const handleDiceRoll = async ({ diceType, isPublic }) => {
    try {
      const { data } = await roomsApi.rollDice(roomId, {
        dice: diceType,
        mode: isPublic ? 'PUBLIC' : 'HIDDEN',
      });
      const diceLabel = formatDice(data.dice ?? diceType);
      if (!isPublic) {
        addEvent({
          type: 'dice',
          text: `Мастер бросил ${diceLabel}: ${data.total ?? data.result} (скрытый)`,
        });
      }
      return { dice: diceLabel, result: data.total ?? data.result };
    } catch (err) {
      addEvent({ type: 'system', text: `Ошибка броска: ${err.message}` });
      return null;
    }
  };

  const handleMagicBall = (answer) => {
    addEvent({ type: 'system', text: `Магический шар: "${answer}"` });
  };

  const handleKick = (participantId) => {
    const p = participants.find((x) => x.participantId === participantId);
    kickParticipant(participantId, p?.userId);
    addEvent({ type: 'leave', text: 'Игрок исключён' });
  };

  const handleCharacterUpdate = async (participantId, data) => {
    const p = participants.find((x) => x.participantId === participantId);
    if (!p?.userId) return;
    const existing = characters[p.userId];
    const optimistic = { ...(existing || {}), ...data, ownerUserId: p.userId };
    setCharacters((prev) => ({ ...prev, [p.userId]: optimistic }));

    const diff = {};
    const old = existing || {};
    for (const key of Object.keys(data || {})) {
      const a = data[key];
      const b = old[key];
      if (a === b) continue;
      if (typeof a === 'object' || typeof b === 'object') {
        try {
          if (JSON.stringify(a) === JSON.stringify(b)) continue;
        } catch { /* fall through and include */ }
      }
      diff[key] = a;
    }

    if (Object.keys(diff).length === 0) return;

    try {
      const characterId = existing?.characterId;
      const { data: saved } = characterId
        ? await charactersApi.updateRoomCharacter(roomId, characterId, diff)
        : await charactersApi.createRoomCharacter(roomId, diff);
      setCharacters((prev) => ({ ...prev, [p.userId]: saved }));
    } catch (err) {
      const status = err?.response?.status;
      const msg = err?.response?.data?.message || err?.message || 'Не удалось сохранить лист';
      console.warn('[session] character update failed:', status, msg, err?.response?.data);
      useToastStore.getState().error(`Лист не сохранён: ${msg}`);
      if (existing) {
        setCharacters((prev) => ({ ...prev, [p.userId]: existing }));
      } else {
        setCharacters((prev) => {
          const next = { ...prev };
          delete next[p.userId];
          return next;
        });
      }
    }
  };

  const handleAttackRoll = ({ player, attack, d20, bonus, attackTotal, damage }) => {
    const bonusStr = bonus >= 0 ? `+${bonus}` : `${bonus}`;
    let text = `${player.characterName || 'Игрок'} — ${attack.name || 'атака'}: d20(${d20})${bonusStr} = ${attackTotal}`;
    if (d20 === 20) text += ' • КРИТ!';
    if (damage) {
      const modStr = damage.mod ? (damage.mod > 0 ? `+${damage.mod}` : `${damage.mod}`) : '';
      text += ` • урон ${damage.formula}: [${damage.rolls.join(', ')}]${modStr} = ${damage.total}`;
    }
    addEvent({ type: 'dice', text });
  };

  const handleEndSession = async ({ winners = [], losers = [] }) => {
    setShowEndModal(false);
    addEvent({ type: 'system', text: 'Сессия завершена!' });
    await finishRoom({ winners, losers });
    try {
      const { data } = await getProfile();
      updateUser(data);
    } catch (err) {
      console.warn('[session] profile refresh after finish failed:', err?.message);
    }
    setTimeout(() => navigate('/'), 1200);
  };

  const players = useMemo(
    () => (participants || [])
      .filter((p) => String(p.role).toLowerCase() !== 'master')
      .map((p) => toUiPlayer(p, characters)),
    [participants, characters],
  );

  if (loading) {
    return <div className="session-loading">Загрузка комнаты…</div>;
  }

  if (error) {
    return (
      <div className="session-loading">
        <p className="sheet-error">{error}</p>
        <button className="btn-secondary" onClick={() => navigate('/')}>На главную</button>
      </div>
    );
  }

  const selectedPlayer = players.find((p) => p.id === selectedParticipantId);

  return (
    <div className="session-page">
      <div className="session-topbar">
        <div className="session-topbar-left">
          <h2 className="session-room-name">
            {currentRoom?.name
              || (currentRoom?.accessMode === 'PUBLIC' ? 'Публичная комната' : 'Приватная комната')}
          </h2>
          <div className="session-code">
            <span>Код:</span>
            <code>{currentRoom?.roomCode || '—'}</code>
            <button className="session-copy-btn" onClick={handleCopyCode}>
              {codeCopied ? <Check size={14} /> : <Copy size={14} />}
            </button>
          </div>
        </div>
        <div className="session-topbar-right">
          <button
            className="btn-secondary session-sync-btn"
            onClick={handleSyncSheets}
            title="Перечитать состояние комнаты и листы персонажей"
          >
            <RefreshCw size={16} />
            Синхронизировать листы
          </button>
          <button className="icon-btn theme-toggle" onClick={toggleTheme}>
            {theme === 'light' ? <Moon size={18} /> : <Sun size={18} />}
          </button>
          <button className="session-end-btn" onClick={() => setShowEndModal(true)}>
            <LogOut size={16} /> Завершить сессию
          </button>
        </div>
      </div>

      <div
        className="session-grid"
        style={{ gridTemplateColumns: `${leftW}px 28px 1fr 28px ${rightW}px` }}
      >
        <div className="session-col-left">
          <div className="session-left-players">
            <PlayerList
              players={players}
              selectedPlayerId={selectedParticipantId}
              onSelect={selectParticipant}
              onKick={handleKick}
            />
          </div>
          <div className="session-left-initiative">
            <InitiativeTracker
              players={players}
              selectedPlayerId={selectedParticipantId}
              onSelectPlayer={selectParticipant}
              onUpdatePlayer={handleCharacterUpdate}
              onLogEvent={addEvent}
            />
          </div>
        </div>

        <div className="session-resizer" onPointerDown={startResize('left')} />

        <div className="session-col-center">
          {selectedPlayer ? (
            <CharacterViewer
              player={selectedPlayer}
              onUpdate={handleCharacterUpdate}
              onAttackRoll={handleAttackRoll}
            />
          ) : (
            <div className="session-no-selection">
              <p>Выберите игрока из списка слева для просмотра листа персонажа</p>
            </div>
          )}
        </div>

        <div className="session-resizer" onPointerDown={startResize('right')} />

        <div className="session-col-right">
          <DicePanel onRoll={handleDiceRoll} />
          <MagicBall onResult={handleMagicBall} />
          <EventLog events={eventLog} />
        </div>
      </div>

      <EndSessionModal
        isOpen={showEndModal}
        onClose={() => setShowEndModal(false)}
        players={players}
        onEnd={handleEndSession}
      />
    </div>
  );
};

export default SessionPage;
