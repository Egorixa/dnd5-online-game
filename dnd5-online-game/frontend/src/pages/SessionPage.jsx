import React, { useEffect, useMemo, useRef, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Copy, LogOut, Check, Sun, Moon } from 'lucide-react';
import useRoomStore from '../stores/roomStore';
import useThemeStore from '../stores/themeStore';
import useAuthStore from '../stores/authStore';
import * as roomsApi from '../api/rooms';
import * as charactersApi from '../api/characters';
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
      window.removeEventListener('mousemove', onMove);
      window.removeEventListener('mouseup', onUp);
      document.body.style.cursor = '';
      document.body.style.userSelect = '';
    };
    document.body.style.cursor = 'col-resize';
    document.body.style.userSelect = 'none';
    window.addEventListener('mousemove', onMove);
    window.addEventListener('mouseup', onUp);
  };

  useEffect(() => {
    fetchRoomState(roomId);
    addEvent({ type: 'system', text: 'Сессия открыта. Ожидание игроков…' });

    charactersApi.listRoomCharacters(roomId).then(({ data }) => {
      const map = {};
      (data.characters || []).forEach((c) => {
        if (c.ownerUserId) map[c.ownerUserId] = c;
      });
      setCharacters(map);
    }).catch(() => {});
  }, [roomId]);

  useEffect(() => {
    let unsubs = [];
    let cancelled = false;

    connectSession(roomId).then((conn) => {
      if (cancelled || !conn) return;
      signalrRef.current = conn;

      unsubs.push(
        onSessionEvent(conn, SESSION_EVENTS.PARTICIPANT_JOINED, (p) => {
          addParticipant(p);
          addEvent({ type: 'join', text: `Игрок ${String(p.userId).slice(0, 6)} присоединился` });
        }),
        onSessionEvent(conn, SESSION_EVENTS.PARTICIPANT_LEFT, ({ participantId }) => {
          removeParticipant(participantId);
          addEvent({ type: 'leave', text: 'Игрок покинул сессию' });
        }),
        onSessionEvent(conn, SESSION_EVENTS.CHARACTER_UPDATED, (data) => {
          if (data?.ownerUserId) {
            setCharacters((prev) => ({ ...prev, [data.ownerUserId]: data }));
          }
          addEvent({ type: 'system', text: 'Лист персонажа обновлён' });
        }),
        onSessionEvent(conn, SESSION_EVENTS.DICE_ROLLED, (roll) => {
          const total = roll.total ?? roll.result;
          const isHidden = roll.mode === 'HIDDEN';
          addEvent({
            type: 'dice',
            text: `Бросок ${roll.dice}: ${total}${isHidden ? ' (скрытый)' : ''}`,
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
      addEvent({
        type: 'dice',
        text: `Мастер бросил ${data.dice}: ${data.total ?? data.result}${isPublic ? '' : ' (скрытый)'}`,
      });
    } catch (err) {
      addEvent({ type: 'system', text: `Ошибка броска: ${err.message}` });
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
    const characterId = characters[p?.userId]?.characterId;
    try {
      if (characterId) {
        const { data: updated } = await charactersApi.updateRoomCharacter(roomId, characterId, data);
        setCharacters((prev) => ({ ...prev, [p.userId]: updated }));
      }
    } catch (err) {
      console.warn('[session] character update failed:', err.message);
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
    if (user) {
      updateUser({ ...user, countMasterTime: (user.countMasterTime || 0) + 1 });
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
        style={{ gridTemplateColumns: `${leftW}px 6px 1fr 6px ${rightW}px` }}
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

        <div className="session-resizer" onMouseDown={startResize('left')} />

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

        <div className="session-resizer" onMouseDown={startResize('right')} />

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
