import React from 'react';
import { SKILLS } from '../../constants/skills';
import { getSkillBonus, formatModifier } from '../../utils/calculations';

const ABILITY_ABBR_RU = {
  strength: 'СИЛ',
  dexterity: 'ЛОВ',
  constitution: 'ТЕЛ',
  intelligence: 'ИНТ',
  wisdom: 'МДР',
  charisma: 'ХАР',
};

const Skills = ({ data, onChange }) => {
  const skills = data.skills || {};

  const toggleSkill = (skillValue) => {
    const updated = { ...skills, [skillValue]: !skills[skillValue] };
    onChange({ ...data, skills: updated });
  };

  return (
    <div className="sheet-section">
      <h3 className="section-title">Навыки</h3>
      <div className="checks-list">
        {SKILLS.map((skill) => {
          const isProficient = !!skills[skill.value];
          const abilityScore = data[skill.ability] || 10;
          const bonus = getSkillBonus(abilityScore, data.level || 1, isProficient);
          return (
            <div key={skill.value} className="check-row">
              <label className="check-label">
                <input
                  type="checkbox"
                  className="proficiency-checkbox"
                  checked={isProficient}
                  onChange={() => toggleSkill(skill.value)}
                />
                <span className="check-bonus">{formatModifier(bonus)}</span>
                <span className="check-name">{skill.label}</span>
                <span className="check-ability">({ABILITY_ABBR_RU[skill.ability] || skill.ability})</span>
              </label>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default Skills;
