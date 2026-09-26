import React from 'react';

const Toast = ({ show, message, title = 'Thành công', type = 'success' }) => {
  if (!show) return null;

  const isSuccess = type === 'success';
  const isError = type === 'error';

  return (
    <div className="fixed bottom-6 right-6 z-50 transform transition-all duration-300 flex items-center gap-3 px-4 py-3 bg-surface-card rounded-xl shadow-2xl border border-border-subtle text-text-primary animate-bounce-short">
      <div
        className={`w-8 h-8 rounded-full flex items-center justify-center shrink-0 ${
          isError ? 'bg-error-container text-error' : 'bg-tertiary-fixed text-tertiary'
        }`}
      >
        <span className="material-symbols-outlined text-[18px]">
          {isError ? 'error' : 'check'}
        </span>
      </div>
      <div className="flex flex-col">
        <span className="font-label-lg text-label-lg text-text-primary">{title}</span>
        <span className="font-body-sm text-body-sm text-text-secondary">{message}</span>
      </div>
    </div>
  );
};

export default Toast;
