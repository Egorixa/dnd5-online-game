import React, { useState } from 'react';
import { Shield, Heart, Eye, UserX } from 'lucide-react';
import Modal from '../ui/Modal';
import Button from '../ui/Button';

const PlayerList = ({ players, selectedPlayerId, onSelect, onKick }) => {
  const [kickTarget, setKickTarget] = useState(null);

  const handleKickConfirm = () => {
    if (kickTarget) {
      onKick(kickTarget.id);
      setKickTarget(null);
    }
  };

  return (
    <div className="player-list">
      <h3 className="panel-title">Игроки ({players.length})</h3>
      <div className="player-cards">
        {players.length === 0 && (
          <p className="player-empty">Ожидание игроков...</p>
        )}
        {players.map((player) => (
          <div
            key={player.id}
            className={`player-card ${selectedPlayerId === player.id ? 'player-card-active' : ''}`}
            onClick={() => onSelect(player.id)}
          >
            <div className="player-card-top">
              <span className="player-name">{player.characterName || 'Без имени'}</span>
              <button
                className="player-kick-btn"
                onClick={(e) => {
                  e.stopPropagation();
                  setKickTarget(player);
                }}
                title="Исключить"
              >
                <UserX size={14} />
              </button>
            </div>
            <div className="player-card-stats">
              <div className="player-stat">
                <Shield size={14} />
                <span>{player.armorClass || 10}</span>
              </div>
              <div className="player-stat">
                <Heart size={14} />
                <span>{player.hpCurrent ?? 0}/{player.hpMax ?? 0}</span>
              </div>
              <div className="player-stat">
                <Eye size={14} />
                <span>{player.passivePerception ?? 10}</span>
              </div>
            </div>
          </div>
        ))}
      </div>

      <Modal
        isOpen={!!kickTarget}
        title="Исключить игрока?"
        onClose={() => setKickTarget(null)}
      >
        <p style={{ marginBottom: '1.5rem' }}>
          Вы уверены, что хотите исключить <strong>{kickTarget?.characterName}</strong> из сессии?
        </p>
        <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end' }}>
          <Button variant="secondary" onClick={() => setKickTarget(null)}>Отмена</Button>
          <Button variant="danger" onClick={handleKickConfirm}>Исключить</Button>
        </div>
      </Modal>
    </div>
  );
};

export default PlayerList;
