// Магический шар: случайный ответ Да/Нет/Возможно.
import React, { useState } from 'react';

const ANSWERS = [
  'Да', 'Нет', 'Возможно', 'Определённо да', 'Скорее нет',
  'Без сомнений', 'Весьма сомнительно', 'Знаки говорят — да',
  'Сконцентрируйся и спроси опять', 'Лучше не рассказывать',
];

const MagicBall = ({ onResult }) => {
  const [answer, setAnswer] = useState(null);
  const [shaking, setShaking] = useState(false);

  const shake = () => {
    setShaking(true);
    setAnswer(null);
    setTimeout(() => {
      const result = ANSWERS[Math.floor(Math.random() * ANSWERS.length)];
      setAnswer(result);
      setShaking(false);
      if (onResult) onResult(result);
    }, 800);
  };

  return (
    <div className="magic-ball-section">
      <button className={`magic-ball-btn ${shaking ? 'magic-shaking' : ''}`} onClick={shake} disabled={shaking}>
        <span className="magic-ball-icon">8</span>
        <span>Магический шар</span>
      </button>
      {answer && <div className="magic-ball-answer">{answer}</div>}
    </div>
  );
};

export default MagicBall;
