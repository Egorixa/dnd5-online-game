import React, { useState } from 'react';
import Modal from '../ui/Modal';
import Button from '../ui/Button';

const EndSessionModal = ({ isOpen, onClose, players, onEnd }) => {
  const [winners, setWinners] = useState([]);
  const [losers, setLosers] = useState([]);

  const toggleWinner = (id) => {
    setWinners((prev) => prev.includes(id) ? prev.filter((w) => w !== id) : [...prev, id]);
    setLosers((prev) => prev.filter((l) => l !== id));
  };

  const toggleLoser = (id) => {
    setLosers((prev) => prev.includes(id) ? prev.filter((l) => l !== id) : [...prev, id]);
    setWinners((prev) => prev.filter((w) => w !== id));
  };

  const handleEnd = () => {
    onEnd({ winners, losers });
    setWinners([]);
    setLosers([]);
  };

  return (
    <Modal isOpen={isOpen} title="Завершить сессию" onClose={onClose}>
      <p style={{ marginBottom: '1rem', color: 'var(--text-secondary)' }}>
        Выберите победителей и проигравших, затем подтвердите завершение.
      </p>

      {players.length > 0 ? (
        <div className="end-session-players">
          {players.map((p) => (
            <div key={p.id} className="end-session-player-row">
              <span>{p.characterName || 'Без имени'}</span>
              <div className="end-session-btns">
                <button
                  className={`end-tag-btn ${winners.includes(p.id) ? 'end-tag-winner' : ''}`}
                  onClick={() => toggleWinner(p.id)}
                >
                  Победа
                </button>
                <button
                  className={`end-tag-btn ${losers.includes(p.id) ? 'end-tag-loser' : ''}`}
                  onClick={() => toggleLoser(p.id)}
                >
                  Поражение
                </button>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <p style={{ color: 'var(--text-secondary)', marginBottom: '1rem' }}>Нет подключённых игроков.</p>
      )}

      <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end', marginTop: '1.5rem' }}>
        <Button variant="secondary" onClick={onClose}>Отмена</Button>
        <Button variant="danger" onClick={handleEnd}>Завершить сессию</Button>
      </div>
    </Modal>
  );
};

export default EndSessionModal;
