import React from 'react';

const Select = ({ label, error, options, register, name, placeholder, ...props }) => {
  return (
    <div className="input-group">
      {label && <label className="input-label">{label}</label>}
      <select
        className={`form-select ${error ? 'input-error' : ''}`}
        {...(register ? register(name) : { name })}
        {...props}
      >
        {placeholder && <option value="">{placeholder}</option>}
        {options.map((opt) => (
          <option key={opt.value} value={opt.value}>
            {opt.label}
          </option>
        ))}
      </select>
      {error && <span className="error-text">{error}</span>}
    </div>
  );
};

export default Select;
