import React from 'react';
import { SAVING_THROWS } from '../../constants/skills';
import { getSavingThrowBonus, formatModifier } from '../../utils/calculations';

const SavingThrows = ({ data, onChange }) => {
  const savingThrows = data.savingThrows || {};

  const toggleProficiency = (ability) => {
    const updated = { ...savingThrows, [ability]: !savingThrows[ability] };
    onChange({ ...data, savingThrows: updated });
  };

  return (
    <div className="sheet-section">
      <h3 className="section-title">Спасброски</h3>
      <div className="checks-list">
        {SAVING_THROWS.map((st) => {
          const isProficient = !!savingThrows[st.value];
          const score = data[st.value] || 10;
          const bonus = getSavingThrowBonus(score, data.level || 1, isProficient);
          return (
            <div key={st.value} className="check-row">
              <label className="check-label">
                <input
                  type="checkbox"
                  className="proficiency-checkbox"
                  checked={isProficient}
                  onChange={() => toggleProficiency(st.value)}
                />
                <span className="check-bonus">{formatModifier(bonus)}</span>
                <span className="check-name">{st.label}</span>
              </label>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default SavingThrows;
