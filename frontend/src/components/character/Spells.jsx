import React from 'react';
import { Plus, Trash2 } from 'lucide-react';
import { canCastSpells, getSpellAbility, getSpellSaveDC, getSpellAttackBonus, formatModifier } from '../../utils/calculations';

const Spells = ({ data, onChange }) => {
  const classValue = data.class;
  if (!canCastSpells(classValue)) return null;

  const spellAbility = getSpellAbility(classValue);
  const abilityScore = data[spellAbility] || 10;
  const level = data.level || 1;
  const saveDC = getSpellSaveDC(level, abilityScore);
  const attackBonus = getSpellAttackBonus(level, abilityScore);

  const spells = data.spells || [];
  const spellSlots = data.spellSlots || {};

  const addSpell = () => {
    if (spells.length >= 100) return;
    onChange({
      ...data,
      spells: [...spells, { name: '', level: 0, school: '', castingTime: '', range: '', components: '', duration: '', description: '', prepared: false }],
    });
  };

  const removeSpell = (index) => {
    onChange({ ...data, spells: spells.filter((_, i) => i !== index) });
  };

  const updateSpell = (index, field, value) => {
    const updated = spells.map((s, i) => i === index ? { ...s, [field]: value } : s);
    onChange({ ...data, spells: updated });
  };

  const updateSlot = (lvl, field, value) => {
    const num = Math.min(99, Math.max(0, parseInt(value) || 0));
    onChange({
      ...data,
      spellSlots: { ...spellSlots, [lvl]: { ...spellSlots[lvl], [field]: num } },
    });
  };

  return (
    <div className="sheet-section">
      <h3 className="section-title">Заклинания</h3>

      <div className="spell-meta">
        <div className="spell-meta-item">
          <span className="spell-meta-label">Базовая характеристика</span>
          <span className="spell-meta-value">{spellAbility}</span>
        </div>
        <div className="spell-meta-item">
          <span className="spell-meta-label">Сложность спасения</span>
          <span className="spell-meta-value computed-field">{saveDC}</span>
        </div>
        <div className="spell-meta-item">
          <span className="spell-meta-label">Бонус атаки</span>
          <span className="spell-meta-value computed-field">{formatModifier(attackBonus)}</span>
        </div>
      </div>

      <h4 style={{ margin: '1.5rem 0 0.5rem' }}>Ячейки заклинаний</h4>
      <div className="spell-slots-grid">
        {[1, 2, 3, 4, 5, 6, 7, 8, 9].map((lvl) => (
          <div key={lvl} className="spell-slot-item">
            <span className="spell-slot-level">Ур. {lvl}</span>
            <input className="form-input spell-slot-input" type="number" min={0} max={99}
              value={spellSlots[lvl]?.total || 0}
              onChange={(e) => updateSlot(lvl, 'total', e.target.value)}
              placeholder="Всего" />
            <input className="form-input spell-slot-input" type="number" min={0} max={99}
              value={spellSlots[lvl]?.used || 0}
              onChange={(e) => updateSlot(lvl, 'used', e.target.value)}
              placeholder="Исп." />
          </div>
        ))}
      </div>

      <h4 style={{ margin: '1.5rem 0 0.5rem' }}>Список заклинаний</h4>
      {spells.map((spell, i) => (
        <div key={i} className="spell-row">
          <div className="spell-row-header">
            <input className="form-input" value={spell.name} maxLength={60}
              onChange={(e) => updateSpell(i, 'name', e.target.value)} placeholder="Название" />
            <select className="form-select spell-level-select"
              value={spell.level} onChange={(e) => updateSpell(i, 'level', parseInt(e.target.value))}>
              {[0, 1, 2, 3, 4, 5, 6, 7, 8, 9].map((l) => (
                <option key={l} value={l}>{l === 0 ? 'Заговор' : `Ур. ${l}`}</option>
              ))}
            </select>
            <label className="check-label spell-prepared">
              <input type="checkbox" checked={!!spell.prepared}
                onChange={() => updateSpell(i, 'prepared', !spell.prepared)} />
              <span>Подг.</span>
            </label>
            <button className="icon-btn" onClick={() => removeSpell(i)}>
              <Trash2 size={16} />
            </button>
          </div>
          <textarea className="form-textarea spell-desc" rows={2} maxLength={1000}
            value={spell.description || ''}
            onChange={(e) => updateSpell(i, 'description', e.target.value)}
            placeholder="Описание заклинания..." />
        </div>
      ))}

      {spells.length < 100 && (
        <button className="btn-secondary add-row-btn" onClick={addSpell}>
          <Plus size={16} /> Добавить заклинание
        </button>
      )}
    </div>
  );
};

export default Spells;
