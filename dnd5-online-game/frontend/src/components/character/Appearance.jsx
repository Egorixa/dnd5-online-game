import React from 'react';

const Appearance = ({ data, onChange }) => {
  const handleChange = (field, value) => {
    onChange({ ...data, [field]: value });
  };

  return (
    <>
      <div className="sheet-section">
        <h3 className="section-title">Внешность</h3>

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

        <div className="input-group" style={{ marginTop: '1rem' }}>
          <label className="input-label">Отличительные черты</label>
          <textarea className="form-textarea" rows={3} maxLength={1500}
            value={data.distinguishingMarks || ''}
            onChange={(e) => handleChange('distinguishingMarks', e.target.value)} />
        </div>
      </div>

      <div className="sheet-section">
        <h3 className="section-title">Предыстория</h3>
        <div className="input-group">
          <textarea className="form-textarea" rows={6} maxLength={3000}
            value={data.backstory || ''}
            onChange={(e) => handleChange('backstory', e.target.value)}
            placeholder="История жизни персонажа..." />
        </div>
      </div>

      <div className="sheet-section">
        <h3 className="section-title">Союзники, сокровища, заметки</h3>
        {[
          { key: 'allies', label: 'Союзники и организации', max: 1500 },
          { key: 'treasure', label: 'Сокровища', max: 1500 },
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
    </>
  );
};

export default Appearance;
