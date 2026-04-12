// Основные сведения: имя, раса, класс, уровень, предыстория, мировоззрение, XP.
import React from 'react';
import { RACES } from '../../constants/races';
import { CLASSES } from '../../constants/classes';
import { BACKGROUNDS } from '../../constants/backgrounds';
import { ALIGNMENTS } from '../../constants/alignments';

const CharacterBasicInfo = ({ data, onChange, errors }) => {
  const handleChange = (field, value) => {
    onChange({ ...data, [field]: value });
  };

  return (
    <div className="sheet-section">
      <h3 className="section-title">Основные сведения</h3>
      <div className="sheet-grid sheet-grid-3">
        <div className="input-group">
          <label className="input-label">Имя персонажа</label>
          <input
            className={`form-input ${errors?.name ? 'input-error' : ''}`}
            value={data.name || ''}
            onChange={(e) => handleChange('name', e.target.value)}
            placeholder="Имя персонажа"
            maxLength={50}
          />
          {errors?.name && <span className="error-text">{errors.name}</span>}
        </div>

        <div className="input-group">
          <label className="input-label">Раса</label>
          <select className="form-select" value={data.race || ''} onChange={(e) => handleChange('race', e.target.value)}>
            <option value="">Выберите расу</option>
            {RACES.map((r) => <option key={r.value} value={r.value}>{r.label}</option>)}
          </select>
        </div>

        <div className="input-group">
          <label className="input-label">Класс</label>
          <select className="form-select" value={data.class || ''} onChange={(e) => handleChange('class', e.target.value)}>
            <option value="">Выберите класс</option>
            {CLASSES.map((c) => <option key={c.value} value={c.value}>{c.label}</option>)}
          </select>
        </div>

        <div className="input-group">
          <label className="input-label">Уровень</label>
          <input className="form-input" type="number" min={1} max={20}
            value={data.level || 1}
            onChange={(e) => handleChange('level', parseInt(e.target.value) || 1)} />
        </div>

        <div className="input-group">
          <label className="input-label">Предыстория</label>
          <select className="form-select" value={data.background || ''} onChange={(e) => handleChange('background', e.target.value)}>
            <option value="">Выберите</option>
            {BACKGROUNDS.map((b) => <option key={b.value} value={b.value}>{b.label}</option>)}
          </select>
        </div>

        <div className="input-group">
          <label className="input-label">Мировоззрение</label>
          <select className="form-select" value={data.alignment || ''} onChange={(e) => handleChange('alignment', e.target.value)}>
            <option value="">Выберите</option>
            {ALIGNMENTS.map((a) => <option key={a.value} value={a.value}>{a.label}</option>)}
          </select>
        </div>

        <div className="input-group">
          <label className="input-label">Очки опыта</label>
          <input className="form-input" type="number" min={0} max={999999}
            value={data.experiencePoints || 0}
            onChange={(e) => handleChange('experiencePoints', parseInt(e.target.value) || 0)} />
        </div>

        <div className="input-group">
          <label className="input-label">Имя игрока</label>
          <input className="form-input"
            value={data.playerName || ''}
            onChange={(e) => handleChange('playerName', e.target.value)}
            placeholder="Имя игрока" maxLength={50} />
        </div>
      </div>
    </div>
  );
};

export default CharacterBasicInfo;
