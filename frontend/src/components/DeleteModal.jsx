import React from 'react';

const DeleteModal = ({ isOpen, onClose, onConfirm, targetName, isLoading }) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-on-surface/40 backdrop-blur-sm animate-in fade-in duration-200">
      <div className="bg-surface-card rounded-2xl max-w-md w-full p-6 sm:p-8 shadow-2xl flex flex-col gap-4 border border-border-subtle animate-in zoom-in-95 duration-150">
        <div className="flex items-center gap-3">
          <div className="w-12 h-12 rounded-xl bg-error-container flex items-center justify-center text-error shrink-0">
            <span className="material-symbols-outlined text-[24px]">warning</span>
          </div>
          <div className="flex flex-col">
            <h3 className="font-headline-sm text-headline-sm text-text-primary">Xác nhận xoá liên kết</h3>
            <span className="font-body-sm text-body-sm text-text-secondary">Hành động này không thể hoàn tác.</span>
          </div>
        </div>

        <p className="font-body-md text-body-md text-text-primary leading-relaxed">
          Bạn có chắc chắn muốn xoá liên kết{' '}
          <code className="font-code-sm text-code-sm px-1.5 py-0.5 rounded bg-surface-card-subtle text-error font-semibold">
            {targetName}
          </code>{' '}
          khỏi hệ thống không? Mọi lượt click chuyển hướng sẽ bị ngắt kết nối vĩnh viễn.
        </p>

        <div className="flex items-center justify-end gap-3 mt-3">
          <button
            onClick={onClose}
            disabled={isLoading}
            type="button"
            className="px-4 py-2.5 rounded-xl bg-surface-card-subtle hover:bg-surface-container text-text-secondary hover:text-text-primary font-label-lg text-label-lg transition-colors cursor-pointer border border-border-subtle"
          >
            Huỷ bỏ
          </button>
          <button
            onClick={onConfirm}
            disabled={isLoading}
            type="button"
            className="px-4 py-2.5 rounded-xl bg-error text-on-error font-label-lg text-label-lg shadow-sm hover:brightness-110 active:scale-98 transition-all cursor-pointer inline-flex items-center gap-2"
          >
            {isLoading ? (
              <svg className="animate-spin h-4 w-4 text-white" viewBox="0 0 24 24" fill="none">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
            ) : (
              <span className="material-symbols-outlined text-[18px]">delete_forever</span>
            )}
            <span>Xác nhận xoá</span>
          </button>
        </div>
      </div>
    </div>
  );
};

export default DeleteModal;
