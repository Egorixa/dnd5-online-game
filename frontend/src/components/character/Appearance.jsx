import React from 'react';

const Appearance = ({ data, onChange }) => {
  const handleChange = (field, value) => {
    onChange({ ...data, [field]: value });
  };

  return (
    <div className="sheet-section">
      <h3 className="section-title">Внешность и предыстория</h3>

      <div className="sheet-grid sheet-grid-3">
        {[
          { key: 'eyes', label: 'Глаза', max: 30 },
          { key: 'skin', label: 'Кожа', max: 30 },
          { key: 'hair', label: 'Волосы', max: 30 },
        ].map((f) => (
          <div key={f.key} className="input-group">
            <label className="input-label">{f.label}</label>
            <input className="form-input" maxLength={f.max}
              value={data[f.key] || ''}
              onChange={(e) => handleChange(f.key, e.target.value)} />
          </div>
        ))}
        {[
          { key: 'age', label: 'Возраст', max: 999 },
          { key: 'height', label: 'Рост', max: 999 },
          { key: 'weight', label: 'Вес', max: 999 },
        ].map((f) => (
          <div key={f.key} className="input-group">
            <label className="input-label">{f.label}</label>
            <input className="form-input" type="number" min={0} max={f.max}
              value={data[f.key] || 0}
              onChange={(e) => handleChange(f.key, Math.min(f.max, parseInt(e.target.value) || 0))} />
          </div>
        ))}
      </div>

      {[
        { key: 'backstory', label: 'Предыстория персонажа', max: 3000 },
        { key: 'allies', label: 'Союзники и организации', max: 1500 },
        { key: 'treasure', label: 'Сокровища', max: 1500 },
        { key: 'distinguishingMarks', label: 'Отличительные черты', max: 1500 },
        { key: 'additionalNotes', label: 'Доп. заметки', max: 1500 },
      ].map((f) => (
        <div key={f.key} className="input-group" style={{ marginTop: '1rem' }}>
          <label className="input-label">{f.label}</label>
          <textarea className="form-textarea" rows={3} maxLength={f.max}
            value={data[f.key] || ''}
            onChange={(e) => handleChange(f.key, e.target.value)} />
        </div>
      ))}
    </div>
  );
};

export default Appearance;
