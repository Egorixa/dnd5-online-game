import React from 'react';

const Button = ({ children, variant = 'primary', disabled, onClick, type = 'button', className = '', ...props }) => {
  const baseClass = variant === 'primary' ? 'btn-main' :
                    variant === 'danger' ? 'btn-danger' :
                    'btn-secondary';

  return (
    <button
      type={type}
      className={`${baseClass} ${className}`}
      disabled={disabled}
      onClick={onClick}
      {...props}
    >
      {children}
    </button>
  );
};

export default Button;
