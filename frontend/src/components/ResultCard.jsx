import React, { useState } from 'react';
import { Link } from 'react-router-dom';

const ResultCard = ({ urlData, onOpenQr, onShowToast }) => {
  const [copied, setCopied] = useState(false);

  if (!urlData) return null;

  const shortUrl = urlData.shortUrl || `https://snaplink.io/${urlData.customAlias || urlData.shortCode}`;

  const handleCopy = () => {
    navigator.clipboard.writeText(shortUrl).then(() => {
      setCopied(true);
      if (onShowToast) {
        onShowToast('Đã sao chép liên kết vào clipboard!', 'check');
      }
      setTimeout(() => setCopied(false), 2400);
    });
  };

  return (
    <div
      id="resultCard"
      className="w-full max-w-4xl mt-6 bg-surface-card rounded-2xl shadow-xl p-5 md:p-6 text-left relative overflow-hidden transition-all duration-300 border border-border-subtle animate-in fade-in slide-in-from-bottom-4 duration-300"
    >
      {/* Glow Emerald Accent Bar on Generation */}
      <div className="absolute top-0 left-0 right-0 h-1.5 bg-gradient-to-r from-status-emerald via-tertiary-fixed to-status-emerald"></div>

      <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        {/* Short link and metadata details */}
        <div className="space-y-1.5 min-w-0 flex-1">
          <div className="flex items-center gap-2">
            <span className="font-label-md text-label-md text-status-emerald font-semibold flex items-center gap-1">
              <span className="material-symbols-outlined text-[16px]">celebration</span>
              🎉 Đường link của bạn đã sẵn sàng!
            </span>
            <span className="px-2 py-0.5 rounded-md text-xs font-medium bg-surface-container-high text-primary">
              {urlData.status || 'Active'}
            </span>
          </div>

          <div className="flex items-center gap-3 pt-1 flex-wrap">
            <a
              href={shortUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="font-headline-sm text-headline-sm text-primary hover:text-brand-violet transition-colors font-mono tracking-tight underline-offset-4 hover:underline truncate max-w-md"
            >
              {shortUrl}
            </a>
            <span className="hidden sm:inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-tertiary-fixed text-on-tertiary-fixed">
              Đã kiểm tra an toàn
            </span>
          </div>

          <p
            className="font-body-sm text-body-sm text-text-secondary truncate max-w-xl"
            title={urlData.originalUrl}
          >
            <span className="font-semibold text-text-muted">Link gốc:</span> {urlData.originalUrl}
          </p>
        </div>

        {/* Mini QR Visual & Interactive Action Buttons */}
        <div className="flex items-center gap-2 w-full md:w-auto shrink-0 pt-2 md:pt-0">
          {/* Clickable Mini QR Preview */}
          <button
            type="button"
            onClick={() => onOpenQr(shortUrl, urlData.customAlias || urlData.shortCode)}
            title="Nhấp để phóng to QR"
            className="cursor-pointer p-2 rounded-xl bg-surface-card-subtle hover:bg-surface-container transition-colors flex items-center justify-center shrink-0 group border border-border-subtle"
          >
            <span className="material-symbols-outlined text-[24px] text-text-primary group-hover:text-brand-violet transition-colors">
              qr_code_2
            </span>
          </button>

          {/* Copy Button with dynamic swap */}
          <button
            type="button"
            onClick={handleCopy}
            className={`flex-1 md:flex-none px-4 py-2.5 rounded-xl font-label-lg text-label-lg flex items-center justify-center gap-1.5 transition-all shadow-sm border border-border-subtle ${
              copied
                ? 'bg-emerald-50 text-status-emerald border-emerald-200'
                : 'bg-surface-card-subtle hover:bg-surface-container text-text-primary'
            }`}
          >
            <span className="material-symbols-outlined text-[18px]">
              {copied ? 'check' : 'content_copy'}
            </span>
            <span>{copied ? 'Đã sao chép' : 'Sao chép'}</span>
          </button>

          {/* QR Modal trigger */}
          <button
            type="button"
            onClick={() => onOpenQr(shortUrl, urlData.customAlias || urlData.shortCode)}
            className="px-3 py-2.5 rounded-xl bg-surface-card-subtle hover:bg-surface-container font-label-lg text-label-lg text-text-secondary hover:text-text-primary flex items-center justify-center gap-1 transition-colors border border-border-subtle"
          >
            <span className="material-symbols-outlined text-[18px]">qr_code_2</span>
            <span className="hidden sm:inline">Mã QR</span>
          </button>

          {/* Analytics Button */}
          {urlData.id ? (
            <Link
              to={`/analytics/${urlData.id}`}
              className="px-3 py-2.5 rounded-xl bg-surface-container hover:bg-surface-container-high font-label-lg text-label-lg text-primary flex items-center justify-center gap-1 transition-colors border border-primary/10"
            >
              <span className="material-symbols-outlined text-[18px]">equalizer</span>
              <span className="hidden sm:inline">Thống kê</span>
            </Link>
          ) : (
            <Link
              to="/dashboard"
              className="px-3 py-2.5 rounded-xl bg-surface-container hover:bg-surface-container-high font-label-lg text-label-lg text-primary flex items-center justify-center gap-1 transition-colors border border-primary/10"
            >
              <span className="material-symbols-outlined text-[18px]">equalizer</span>
              <span className="hidden sm:inline">Dashboard</span>
            </Link>
          )}
        </div>
      </div>
    </div>
  );
};

export default ResultCard;
