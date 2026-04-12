// Панель кубиков (d4-d100) с публичным/скрытым режимом броска.
import React, { useState } from 'react';
import { Dice1, EyeOff, Eye } from 'lucide-react';

const DICE = [
  { type: 'd4', sides: 4 },
  { type: 'd6', sides: 6 },
  { type: 'd8', sides: 8 },
  { type: 'd10', sides: 10 },
  { type: 'd12', sides: 12 },
  { type: 'd20', sides: 20 },
  { type: 'd100', sides: 100 },
];

const DicePanel = ({ onRoll }) => {
  const [isPublic, setIsPublic] = useState(true);
  const [lastResult, setLastResult] = useState(null);
  const [rolling, setRolling] = useState(false);

  const rollDice = (sides, type) => {
    setRolling(true);
    setTimeout(() => {
      const result = Math.floor(Math.random() * sides) + 1;
      setLastResult({ type, result });
      setRolling(false);
      if (onRoll) {
        onRoll({ diceType: type, result, isPublic });
      }
    }, 300);
  };

  return (
    <div className="dice-panel">
      <h3 className="panel-title">Кубики</h3>
      <div className="dice-grid">
        {DICE.map((d) => (
          <button
            key={d.type}
            className={`dice-btn ${rolling ? 'dice-rolling' : ''}`}
            onClick={() => rollDice(d.sides, d.type)}
            disabled={rolling}
          >
            {d.type}
          </button>
        ))}
      </div>

      <div className="dice-mode-toggle">
        <button
          className={`dice-mode-btn ${isPublic ? 'dice-mode-active' : ''}`}
          onClick={() => setIsPublic(true)}
        >
          <Eye size={14} /> Публичный
        </button>
        <button
          className={`dice-mode-btn ${!isPublic ? 'dice-mode-active' : ''}`}
          onClick={() => setIsPublic(false)}
        >
          <EyeOff size={14} /> Скрытый
        </button>
      </div>

      {lastResult && (
        <div className={`dice-result ${lastResult.result === 20 && lastResult.type === 'd20' ? 'dice-crit' : ''} ${lastResult.result === 1 && lastResult.type === 'd20' ? 'dice-fail' : ''}`}>
          <span className="dice-result-type">{lastResult.type}</span>
          <span className="dice-result-value">{lastResult.result}</span>
        </div>
      )}
    </div>
  );
};

export default DicePanel;
