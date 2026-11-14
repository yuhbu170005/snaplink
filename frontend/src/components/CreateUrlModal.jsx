/** Form có kiểm soát: Dashboard giữ state/API, modal chỉ hiển thị và phát sự kiện. */
export default function CreateUrlModal({ isOpen, form, onChange, onClose, onSubmit, isSubmitting }) {
  if (!isOpen) return null;
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-on-surface/40 backdrop-blur-sm animate-in fade-in duration-150">
      <div className="bg-surface-card rounded-2xl max-w-lg w-full p-6 sm:p-8 shadow-2xl flex flex-col gap-4 border border-border-subtle">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-lg bg-surface-container flex items-center justify-center text-brand-violet">
              <span className="material-symbols-outlined text-[20px]">add_link</span>
            </div>
            <h3 className="font-display text-xl font-bold text-text-primary">
              Tạo Short Link Mới
            </h3>
          </div>
          <button
            type="button"
            onClick={onClose}
            disabled={isSubmitting}
            className="text-text-muted hover:text-text-primary p-1 rounded-lg"
          >
            <span className="material-symbols-outlined">close</span>
          </button>
        </div>

        <form className="flex flex-col gap-4" onSubmit={onSubmit}>
          <div className="flex flex-col gap-1.5">
            <label className="font-label-md text-label-md text-text-secondary">
              Đường dẫn gốc (Original URL) *
            </label>
            <div className="flex items-center bg-surface-card-subtle px-3 py-2.5 rounded-xl border border-border-subtle focus-within:border-brand-violet focus-within:bg-surface-card shadow-sm">
              <span className="material-symbols-outlined text-text-muted mr-2 text-[18px]">link</span>
              <input
                type="url"
                required
                value={form.originalUrl}
                onChange={(e) => onChange({ ...form, originalUrl: e.target.value })}
                placeholder="https://example.com/very-long-url-path..."
                className="w-full bg-transparent border-0 outline-none font-body-sm text-body-sm text-text-primary"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="flex flex-col gap-1.5">
              <label className="font-label-md text-label-md text-text-secondary">
                Custom Alias (Tuỳ chọn)
              </label>
              <div className="flex items-center bg-surface-card-subtle px-2 py-2 rounded-xl border border-border-subtle focus-within:border-brand-violet focus-within:bg-surface-card shadow-sm">
                <span className="font-mono text-xs text-text-muted pr-1">Alias:</span>
                <input
                  type="text"
                  value={form.customAlias}
                  onChange={(e) => onChange({ ...form, customAlias: e.target.value })}
                  placeholder="my-alias"
                  className="w-full bg-transparent border-0 outline-none font-mono text-xs text-text-primary"
                />
              </div>
            </div>

            <div className="flex flex-col gap-1.5">
              <label className="font-label-md text-label-md text-text-secondary">
                Hạn sử dụng (Tuỳ chọn)
              </label>
              <div className="flex items-center bg-surface-card-subtle px-3 py-2 rounded-xl border border-border-subtle focus-within:border-brand-violet focus-within:bg-surface-card shadow-sm">
                <input
                  type="datetime-local"
                  value={form.expiresAt}
                  onChange={(e) => onChange({ ...form, expiresAt: e.target.value })}
                  className="w-full bg-transparent border-0 outline-none font-body-sm text-body-sm text-text-primary"
                />
              </div>
            </div>
          </div>

          <div className="flex items-center justify-end gap-3 mt-4">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2.5 rounded-xl bg-surface-card-subtle hover:bg-surface-container text-text-secondary font-label-lg text-label-lg border border-border-subtle cursor-pointer"
            >
              Đóng
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="px-5 py-2.5 rounded-xl text-on-primary font-label-lg text-label-lg shadow-md hover:brightness-105 bg-brand-violet cursor-pointer disabled:opacity-75"
            >
              {isSubmitting ? 'Đang tạo...' : 'Tạo Ngay'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
