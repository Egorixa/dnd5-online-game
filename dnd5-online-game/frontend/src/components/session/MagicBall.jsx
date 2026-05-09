import React, { useState } from 'react';

const MagicBall = ({ onAsk }) => {
  const [answer, setAnswer] = useState(null);
  const [shaking, setShaking] = useState(false);

  const shake = async () => {
    if (!onAsk) return;
    setShaking(true);
    setAnswer(null);
    try {
      const result = await onAsk();
      if (result) setAnswer(result);
    } finally {
      setShaking(false);
    }
  };

  return (
    <div className="magic-ball-section">
      <button
        type="button"
        className={`magic-ball-btn ${shaking ? 'magic-shaking' : ''}`}
        onClick={shake}
        disabled={shaking}
      >
        <span className="magic-ball-icon">8</span>
        <span>Магический шар</span>
      </button>
      {answer && <div className="magic-ball-answer">{answer}</div>}
    </div>
  );
};

export default MagicBall;
