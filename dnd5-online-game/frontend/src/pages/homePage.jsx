import React, { useState } from 'react';

const HomePage = () => {
    const [access, setAccess] = useState('Private');

    const handleCreateRoom = () => {
        console.log("Room created with access:", access);
    };

    return (
        <div className="hero-section">
            <h1 className="hero-title">Добро пожаловать в сервис для игры в DnD5!</h1>
            <p className="hero-subtitle">
                Создайте комнату и объедините друзей и героев для проведения великой игры.
            </p>

            <div className="config-card">
                <div className="input-group">
                    <label className="input-label">Доступ к комнате</label>
                    <select value={access} onChange={(e) => setAccess(e.target.value)}>
                        <option value="Public">Публичная (видна в поиске)</option>
                        <option value="Private">Приватная (только по коду)</option>
                    </select>
                </div>

                <button className="btn-main" onClick={handleCreateRoom}>
                    Создать новую комнату
                </button>

                <p style={{ fontSize: '0.9rem', color: 'var(--text-secondary)', marginTop: '1.5rem' }}>
                    После нажатия кнопки вы получите уникальный код для приглашения игроков.
                </p>
            </div>
        </div>
    );
};

export default HomePage;