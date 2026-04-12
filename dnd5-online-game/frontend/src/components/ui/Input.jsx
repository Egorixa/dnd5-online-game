// Текстовое поле с лейблом, ошибкой и toggle показа пароля.
import React, { useState } from 'react';
import { Eye, EyeOff } from 'lucide-react';

const Input = ({ label, error, type = 'text', register, name, placeholder, ...props }) => {
  const [showPassword, setShowPassword] = useState(false);
  const isPassword = type === 'password';
  const inputType = isPassword && showPassword ? 'text' : type;

  return (
    <div className="input-group">
      {label && <label className="input-label">{label}</label>}
      <div className="input-wrapper">
        <input
          className={`form-input ${error ? 'input-error' : ''}`}
          type={inputType}
          placeholder={placeholder}
          {...(register ? register(name) : { name })}
          {...props}
        />
        {isPassword && (
          <button
            type="button"
            className="password-toggle"
            onClick={() => setShowPassword(!showPassword)}
            tabIndex={-1}
          >
            {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
          </button>
        )}
      </div>
      {error && <span className="error-text">{error}</span>}
    </div>
  );
};

export default Input;
