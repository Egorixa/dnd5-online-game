// Экран сессии мастера: 3-колоночный layout (игроки + трекер | лист персонажа | кубики + лог).
import React, { useEffect, useRef, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Copy, LogOut, Check, Sun, Moon } from 'lucide-react';
import useRoomStore from '../stores/roomStore';
import useThemeStore from '../stores/themeStore';
import useAuthStore from '../stores/authStore';
import { incrementMasterGames } from '../api/auth';
import {
  connectSession, onSessionEvent, sendSessionAction, disconnectSession,
  SESSION_EVENTS, SESSION_ACTIONS,
} from '../api/signalr';
import PlayerList from '../components/session/PlayerList';
import DicePanel from '../components/session/DicePanel';
import MagicBall from '../components/session/MagicBall';
import EventLog from '../components/session/EventLog';
import InitiativeTracker from '../components/session/InitiativeTracker';
import EndSessionModal from '../components/session/EndSessionModal';
import CharacterViewer from '../components/session/CharacterViewer';

const SessionPage = () => {
  const { roomId } = useParams();
  const navigate = useNavigate();
  const {
    currentRoom, players, selectedPlayerId, eventLog,
    loading, error,
    fetchRoom, selectPlayer, addEvent, addPlayer, removePlayer, updatePlayer, leaveRoom,
  } = useRoomStore();

  const channelRef = useRef(null);
  const signalrRef = useRef(null);

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
  const [showEndModal, setShowEndModal] = useState(false);
  const [codeCopied, setCodeCopied] = useState(false);

  useEffect(() => {
    fetchRoom(roomId);
    addEvent({ type: 'system', text: 'Сессия создана. Ожидание игроков...' });
  }, [roomId]);

  useEffect(() => {
    const channel = new BroadcastChannel('dnd5-session');
    channelRef.current = channel;

    channel.onmessage = (event) => {
      const msg = event.data;

      switch (msg.type) {
        case 'player:joined':
          addPlayer(msg.player);
          addEvent({ type: 'join', text: `${msg.player.characterName || msg.player.username} присоединился к сессии` });
          break;
        case 'player:left':
          const leavingPlayer = useRoomStore.getState().players.find((p) => p.id === msg.playerId);
          removePlayer(msg.playerId);
          addEvent({ type: 'leave', text: `${leavingPlayer?.characterName || 'Игрок'} покинул сессию` });
          break;
        case 'character:updated':
          updatePlayer(msg.player.id, msg.player);
          addEvent({ type: 'system', text: `${msg.player.characterName || 'Игрок'} обновил лист персонажа` });
          break;
        case 'room:discover':
          const room = useRoomStore.getState().currentRoom;
          if (room && room.accessType === 'Public') {
            channel.postMessage({
              type: 'room:info',
              room: { id: room.id, name: room.name, inviteCode: room.inviteCode, accessType: room.accessType },
            });
          }
          break;
        default:
          break;
      }
    };

    const room = useRoomStore.getState().currentRoom;
    if (room && room.accessType === 'Public') {
      channel.postMessage({
        type: 'room:info',
        room: { id: room.id, name: room.name, inviteCode: room.inviteCode, accessType: room.accessType },
      });
    }

    return () => {
      channel.close();
      channelRef.current = null;
    };
  }, []);

  useEffect(() => {
    let unsubs = [];
    let cancelled = false;

    connectSession(roomId).then((conn) => {
      if (cancelled || !conn) return;
      signalrRef.current = conn;

      unsubs.push(
        onSessionEvent(conn, SESSION_EVENTS.PLAYER_JOINED, (player) => {
          addPlayer(player);
          addEvent({ type: 'join', text: `${player.characterName || player.username} присоединился к сессии` });
        }),
        onSessionEvent(conn, SESSION_EVENTS.PLAYER_LEFT, ({ playerId }) => {
          const p = useRoomStore.getState().players.find((pl) => pl.id === playerId);
          removePlayer(playerId);
          addEvent({ type: 'leave', text: `${p?.characterName || 'Игрок'} покинул сессию` });
        }),
        onSessionEvent(conn, SESSION_EVENTS.PLAYER_KICKED, ({ playerId }) => {
          removePlayer(playerId);
          addEvent({ type: 'leave', text: 'Игрок исключён из сессии' });
        }),
        onSessionEvent(conn, SESSION_EVENTS.CHARACTER_UPDATED, (data) => {
          updatePlayer(data.id, data);
          addEvent({ type: 'system', text: `${data.characterName || 'Игрок'} обновил лист персонажа` });
        }),
        onSessionEvent(conn, SESSION_EVENTS.DICE_ROLLED, ({ username, diceType, result, isPublic }) => {
          addEvent({ type: 'dice', text: `${username} бросил ${diceType}: ${result}${isPublic ? '' : ' (скрытый)'}` });
        }),
        onSessionEvent(conn, SESSION_EVENTS.SESSION_ENDED, () => {
          addEvent({ type: 'system', text: 'Сессия завершена мастером' });
          setTimeout(() => { leaveRoom(); navigate('/'); }, 1500);
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
    const code = currentRoom?.inviteCode || roomId;
    navigator.clipboard.writeText(code).then(() => {
      setCodeCopied(true);
      setTimeout(() => setCodeCopied(false), 2000);
    });
  };

  const handleDiceRoll = ({ diceType, result, isPublic }) => {
    addEvent({
      type: 'dice',
      text: `Мастер бросил ${diceType}: ${result}${isPublic ? '' : ' (скрытый)'}`,
    });
    sendSessionAction(signalrRef.current, SESSION_ACTIONS.ROLL_DICE, roomId, diceType, result, isPublic);
  };

  const handleMagicBall = (answer) => {
    addEvent({ type: 'system', text: `Магический шар: "${answer}"` });
  };

  const handleKickPlayer = (playerId) => {
    const player = players.find((p) => p.id === playerId);
    removePlayer(playerId);
    addEvent({ type: 'leave', text: `${player?.characterName || 'Игрок'} исключён из сессии` });
    channelRef.current?.postMessage({ type: 'player:kicked', playerId });
    sendSessionAction(signalrRef.current, SESSION_ACTIONS.KICK_PLAYER, roomId, playerId);
  };

  const handleCharacterUpdate = (playerId, data) => {
    updatePlayer(playerId, data);
    channelRef.current?.postMessage({ type: 'character:updated', player: { ...data, id: playerId } });
    sendSessionAction(signalrRef.current, SESSION_ACTIONS.UPDATE_CHARACTER, roomId, { ...data, id: playerId });
  };

  const handleAttackRoll = ({ player, attack, d20, bonus, attackTotal, damage }) => {
    const attackName = attack.name || 'атака';
    const bonusStr = bonus >= 0 ? `+${bonus}` : `${bonus}`;
    let text = `${player.characterName || 'Игрок'} — ${attackName}: d20(${d20})${bonusStr} = ${attackTotal}`;
    if (d20 === 20) text += ' • КРИТ!';
    if (damage) {
      const modStr = damage.mod ? (damage.mod > 0 ? `+${damage.mod}` : `${damage.mod}`) : '';
      text += ` • урон ${damage.formula}: [${damage.rolls.join(', ')}]${modStr} = ${damage.total}`;
    } else if (attack.damage) {
      text += ` • урон: ${attack.damage}`;
    }
    addEvent({ type: 'dice', text });
  };

  const handleEndSession = async ({ winners, losers }) => {
    setShowEndModal(false);
    addEvent({ type: 'system', text: 'Сессия завершена!' });
    channelRef.current?.postMessage({ type: 'session:ended', winners, losers });
    sendSessionAction(signalrRef.current, SESSION_ACTIONS.END_SESSION, roomId, winners, losers);

    try {
      const { data } = await incrementMasterGames();
      updateUser(data);
    } catch {
      if (user) {
        updateUser({ ...user, countMasterTime: (user.countMasterTime || 0) + 1 });
      }
    }

    setTimeout(() => {
      leaveRoom();
      navigate('/');
    }, 1500);
  };

  if (loading) {
    return <div className="session-loading">Загрузка комнаты...</div>;
  }

  if (error) {
    return (
      <div className="session-loading">
        <p className="sheet-error">{error}</p>
        <button className="btn-secondary" onClick={() => navigate('/')}>На главную</button>
      </div>
    );
  }

  const selectedPlayer = players.find((p) => p.id === selectedPlayerId);

  return (
    <div className="session-page">
      {/* Top bar */}
      <div className="session-topbar">
        <div className="session-topbar-left">
          <h2 className="session-room-name">{currentRoom?.name || 'Комната'}</h2>
          <div className="session-code">
            <span>Код:</span>
            <code>{currentRoom?.inviteCode || '—'}</code>
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

      {/* 3-column layout — columns are resizable */}
      <div
        className="session-grid"
        style={{ gridTemplateColumns: `${leftW}px 6px 1fr 6px ${rightW}px` }}
      >
        {/* Left: scrollable PlayerList + pinned InitiativeTracker */}
        <div className="session-col-left">
          <div className="session-left-players">
            <PlayerList
              players={players}
              selectedPlayerId={selectedPlayerId}
              onSelect={selectPlayer}
              onKick={handleKickPlayer}
            />
          </div>
          <div className="session-left-initiative">
            <InitiativeTracker
              players={players}
              selectedPlayerId={selectedPlayerId}
              onSelectPlayer={selectPlayer}
              onUpdatePlayer={handleCharacterUpdate}
              onLogEvent={addEvent}
            />
          </div>
        </div>

        <div
          className="session-resizer"
          onMouseDown={startResize('left')}
          title="Потяните, чтобы изменить ширину"
        />

        {/* Center: Character Viewer */}
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

        <div
          className="session-resizer"
          onMouseDown={startResize('right')}
          title="Потяните, чтобы изменить ширину"
        />

        {/* Right: Tools */}
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
