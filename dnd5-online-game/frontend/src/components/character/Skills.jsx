import React from 'react';
import { SKILLS } from '../../constants/skills';
import { getSkillBonus, formatModifier } from '../../utils/calculations';

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
                <span className="check-ability">({skill.ability.slice(0, 3).toUpperCase()})</span>
              </label>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default Skills;
