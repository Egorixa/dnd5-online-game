import React from 'react';
import { SAVING_THROWS } from '../../constants/skills';
import { getSavingThrowBonus, formatModifier } from '../../utils/calculations';

const normLevel = (v) => {
  if (v === true) return 'Proficient';
  if (v === 'Expertise' || v === 'Proficient') return v;
  return 'None';
};

const SavingThrows = ({ data, onChange }) => {
  const savingThrows = data.savingThrows || {};

  const setLevel = (ability, level) => {
    const updated = { ...savingThrows };
    if (level === 'None') delete updated[ability];
    else updated[ability] = level;
    onChange({ ...data, savingThrows: updated });
  };

  return (
    <div className="sheet-section">
      <h3 className="section-title">Спасброски</h3>
      <div className="checks-list">
        {SAVING_THROWS.map((st) => {
          const level = normLevel(savingThrows[st.value]);
          const score = data[st.value] || 10;
          const bonus = getSavingThrowBonus(score, data.level || 1, level);
          return (
            <div key={st.value} className="check-row">
              <select
                className="form-select proficiency-select"
                value={level}
                onChange={(e) => setLevel(st.value, e.target.value)}
              >
                <option value="None">Нет</option>
                <option value="Proficient">Владение</option>
                <option value="Expertise">Мастерство</option>
              </select>
              <span className="check-bonus">{formatModifier(bonus)}</span>
              <span className="check-name">{st.label}</span>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default SavingThrows;
