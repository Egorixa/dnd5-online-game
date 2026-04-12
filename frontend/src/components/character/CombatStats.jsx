// Боевые параметры: КД, HP (с cap), скорость, кость хитов, спасброски от смерти.
import React from 'react';
import { getHitDie } from '../../utils/calculations';

const CombatStats = ({ data, onChange }) => {
  const handleChange = (field, value) => {
    onChange({ ...data, [field]: value });
  };

  const handleNumber = (field, value, min, max) => {
    const num = Math.min(max, Math.max(min, parseInt(value) || min));
    handleChange(field, num);
  };

  const hitDie = getHitDie(data.class);

  return (
    <div className="sheet-section">
      <h3 className="section-title">Боевые параметры</h3>

      <div className="combat-grid">
        <div className="combat-stat-card">
          <span className="combat-label">Класс Доспеха</span>
          <input className="combat-input" type="number" min={1} max={50}
            value={data.armorClass || 10}
            onChange={(e) => handleNumber('armorClass', e.target.value, 1, 50)} />
        </div>

        <div className="combat-stat-card">
          <span className="combat-label">Инициатива</span>
          <input className="combat-input" type="number" min={-20} max={20}
            value={data.initiativeBonus || 0}
            onChange={(e) => handleNumber('initiativeBonus', e.target.value, -20, 20)} />
        </div>

        <div className="combat-stat-card">
          <span className="combat-label">Скорость</span>
          <input className="combat-input" type="number" min={0} max={200}
            value={data.speed || 30}
            onChange={(e) => handleNumber('speed', e.target.value, 0, 200)} />
        </div>
      </div>

      <div className="hp-section">
        <div className="sheet-grid sheet-grid-3">
          <div className="input-group">
            <label className="input-label">Макс. хиты</label>
            <input className="form-input" type="number" min={1} max={999}
              value={data.hpMax || 10}
              onChange={(e) => handleNumber('hpMax', e.target.value, 1, 999)} />
          </div>
          <div className="input-group">
            <label className="input-label">Текущие хиты</label>
            <input className="form-input" type="number" min={0} max={data.hpMax || 999}
              value={data.hpCurrent || 0}
              onChange={(e) => handleNumber('hpCurrent', e.target.value, 0, data.hpMax || 999)} />
          </div>
          <div className="input-group">
            <label className="input-label">Временные хиты</label>
            <input className="form-input" type="number" min={0} max={999}
              value={data.hpTemp || 0}
              onChange={(e) => handleNumber('hpTemp', e.target.value, 0, 999)} />
          </div>
        </div>

        <div className="sheet-grid sheet-grid-3" style={{ marginTop: '1rem' }}>
          <div className="input-group">
            <label className="input-label">Кость хитов</label>
            <input className="form-input computed-field" value={hitDie} readOnly />
          </div>
          <div className="input-group">
            <label className="input-label">Всего костей</label>
            <input className="form-input computed-field" value={data.level || 1} readOnly />
          </div>
          <div className="input-group">
            <label className="input-label">Текущие кости</label>
            <input className="form-input" type="number" min={0} max={data.level || 1}
              value={data.hitDiceCurrent ?? data.level ?? 1}
              onChange={(e) => handleNumber('hitDiceCurrent', e.target.value, 0, data.level || 1)} />
          </div>
        </div>
      </div>

      <div className="death-saves-section">
        <h4>Спасброски от смерти</h4>
        <div className="death-saves-row">
          <span>Успехи:</span>
          {[1, 2, 3].map((i) => (
            <input key={`s${i}`} type="checkbox"
              checked={(data.deathSavesSuccesses || 0) >= i}
              onChange={() => handleChange('deathSavesSuccesses',
                (data.deathSavesSuccesses || 0) >= i ? i - 1 : i)} />
          ))}
        </div>
        <div className="death-saves-row">
          <span>Провалы:</span>
          {[1, 2, 3].map((i) => (
            <input key={`f${i}`} type="checkbox"
              checked={(data.deathSavesFailures || 0) >= i}
              onChange={() => handleChange('deathSavesFailures',
                (data.deathSavesFailures || 0) >= i ? i - 1 : i)} />
          ))}
        </div>
      </div>

      <div className="inspiration-row">
        <label className="check-label">
          <input type="checkbox"
            checked={!!data.inspiration}
            onChange={() => handleChange('inspiration', !data.inspiration)} />
          <span>Вдохновение</span>
        </label>
      </div>
    </div>
  );
};

export default CombatStats;
