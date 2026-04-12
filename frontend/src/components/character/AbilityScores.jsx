// Секция 6 характеристик с модификаторами и бонусом мастерства.
import React from 'react';
import { ABILITIES } from '../../constants/skills';
import { getModifier, formatModifier } from '../../utils/calculations';
import { getProficiencyBonus } from '../../constants/proficiencyBonus';

const AbilityScores = ({ data, onChange }) => {
  const handleChange = (ability, value) => {
    const num = Math.min(30, Math.max(1, parseInt(value) || 1));
    onChange({ ...data, [ability]: num });
  };

  const level = data.level || 1;
  const profBonus = getProficiencyBonus(level);

  return (
    <div className="sheet-section">
      <h3 className="section-title">Характеристики</h3>
      <p className="section-hint">Бонус мастерства: <strong>{formatModifier(profBonus)}</strong></p>
      <div className="abilities-grid">
        {ABILITIES.map((ab) => {
          const score = data[ab.value] || 10;
          const mod = getModifier(score);
          return (
            <div key={ab.value} className="ability-card">
              <span className="ability-name">{ab.label}</span>
              <span className="ability-name-en">{ab.labelEn}</span>
              <input
                className="ability-score-input"
                type="number" min={1} max={30}
                value={score}
                onChange={(e) => handleChange(ab.value, e.target.value)}
              />
              <span className="ability-modifier">{formatModifier(mod)}</span>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default AbilityScores;
