import React from 'react';

const FIELDS = [
  { key: 'personalityTraits', label: 'Черты характера', max: 1500 },
  { key: 'ideals', label: 'Идеалы', max: 1500 },
  { key: 'bonds', label: 'Привязанности', max: 1500 },
  { key: 'flaws', label: 'Слабости', max: 1500 },
  { key: 'featuresAndTraits', label: 'Особенности и умения', max: 3000 },
];

const PersonalityTraits = ({ data, onChange }) => {
  const handleChange = (field, value) => {
    onChange({ ...data, [field]: value });
  };

  return (
    <div className="sheet-section">
      <h3 className="section-title">Черты личности</h3>
      {FIELDS.map((f) => (
        <div key={f.key} className="input-group" style={{ marginBottom: '1rem' }}>
          <label className="input-label">{f.label}</label>
          <textarea className="form-textarea" rows={3} maxLength={f.max}
            value={data[f.key] || ''}
            onChange={(e) => handleChange(f.key, e.target.value)} />
        </div>
      ))}
    </div>
  );
};

export default PersonalityTraits;
