import React from 'react';
import useToastStore from '../../stores/toastStore';
import Toast from './Toast';

const ToastContainer = () => {
  const toasts = useToastStore((s) => s.toasts);
  const remove = useToastStore((s) => s.remove);

  if (!toasts.length) return null;

  return (
    <div className="toast-container">
      {toasts.map((t) => (
        <Toast
          key={t.id}
          message={t.message}
          type={t.type}
          duration={t.duration}
          onClose={() => remove(t.id)}
        />
      ))}
    </div>
  );
};

export default ToastContainer;
