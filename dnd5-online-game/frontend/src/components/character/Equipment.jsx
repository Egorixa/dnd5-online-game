// Снаряжение, прочие владения/языки и валюта (5 типов монет).
import React from 'react';

const CURRENCIES = [
  { key: 'copper', label: 'ММ (медные)' },
  { key: 'silver', label: 'СМ (серебряные)' },
  { key: 'electrum', label: 'ЭМ (электрум)' },
  { key: 'gold', label: 'ЗМ (золотые)' },
  { key: 'platinum', label: 'ПМ (платиновые)' },
];

const Equipment = ({ data, onChange }) => {
  const handleChange = (field, value) => {
    onChange({ ...data, [field]: value });
  };

  return (
    <div className="sheet-section">
      <h3 className="section-title">Снаряжение и валюта</h3>

      <div className="input-group">
        <label className="input-label">Снаряжение</label>
        <textarea className="form-textarea" rows={4} maxLength={2000}
          value={data.equipment || ''}
          onChange={(e) => handleChange('equipment', e.target.value)}
          placeholder="Список снаряжения..." />
      </div>

      <div className="input-group" style={{ marginTop: '1rem' }}>
        <label className="input-label">Прочие владения и языки</label>
        <textarea className="form-textarea" rows={3} maxLength={1000}
          value={data.proficienciesAndLanguages || ''}
          onChange={(e) => handleChange('proficienciesAndLanguages', e.target.value)}
          placeholder="Языки, инструменты..." />
      </div>

      <div className="currency-grid">
        {CURRENCIES.map((c) => (
          <div key={c.key} className="input-group">
            <label className="input-label">{c.label}</label>
            <input className="form-input" type="number" min={0} max={999999}
              value={data[c.key] || 0}
              onChange={(e) => handleChange(c.key, Math.min(999999, parseInt(e.target.value) || 0))} />
          </div>
        ))}
      </div>
    </div>
  );
};

export default Equipment;
