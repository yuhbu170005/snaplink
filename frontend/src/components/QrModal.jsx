import React, { useRef } from 'react';
import { QRCodeSVG } from 'qrcode.react';

const QrModal = ({ isOpen, onClose, url, shortCode, onCopySuccess }) => {
  const qrRef = useRef(null);

  if (!isOpen) return null;

  const handleCopy = () => {
    navigator.clipboard.writeText(url);
    if (onCopySuccess) onCopySuccess();
  };

  const handleDownloadPng = () => {
    const svg = qrRef.current.querySelector('svg');
    if (!svg) return;

    const svgData = new XMLSerializer().serializeToString(svg);
    const canvas = document.createElement('canvas');
    const ctx = canvas.getContext('2d');
    const img = new Image();

    canvas.width = 512;
    canvas.height = 512;

    img.onload = () => {
      ctx.fillStyle = '#FFFFFF';
      ctx.fillRect(0, 0, canvas.width, canvas.height);
      ctx.drawImage(img, 32, 32, 448, 448);
      const pngFile = canvas.toDataURL('image/png');
      const downloadLink = document.createElement('a');
      downloadLink.download = `snaplink-qr-${shortCode || 'code'}.png`;
      downloadLink.href = pngFile;
      downloadLink.click();
    };

    img.src = 'data:image/svg+xml;base64,' + btoa(unescape(encodeURIComponent(svgData)));
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-on-background/50 backdrop-blur-sm animate-in fade-in duration-200">
      <div className="bg-surface-card rounded-2xl max-w-md w-full p-6 sm:p-8 shadow-2xl relative flex flex-col items-center text-center border border-border-subtle">
        {/* Close button */}
        <button
          onClick={onClose}
          className="absolute top-4 right-4 p-2 text-text-muted hover:text-on-surface hover:bg-surface-card-subtle rounded-xl transition-colors"
          aria-label="Đóng modal"
        >
          <span className="material-symbols-outlined text-[20px]">close</span>
        </button>

        {/* Header Icon */}
        <div className="w-12 h-12 rounded-xl bg-surface-container flex items-center justify-center text-brand-violet mb-3">
          <span className="material-symbols-outlined text-[26px]">qr_code_2</span>
        </div>

        <h3 className="font-headline-sm text-headline-sm text-text-primary mb-1">Mã QR Động</h3>
        <p className="font-body-sm text-body-sm text-text-secondary mb-5">
          Quét mã để truy cập tức thì hoặc tải ảnh về cho ấn phẩm truyền thông
        </p>

        {/* QR Code Container */}
        <div
          ref={qrRef}
          className="p-5 bg-surface-card-subtle rounded-2xl flex items-center justify-center mb-5 shadow-inner border border-border-subtle"
        >
          <QRCodeSVG
            value={url || 'https://snaplink.io'}
            size={180}
            level="H"
            fgColor="#0F172A"
            bgColor="transparent"
            imageSettings={{
              src: 'https://lh3.googleusercontent.com/aida/AEtjO1UCM6d_3vHWl1Kx6GEhf56_JHJ3m7u5-fU0w-tbcjc9lvD9LqVRKjqx7qTjvouSuvIYs9HruUHOfdixBISS7XtahR0aTV5xGdodeoOxdlV80rLqgRT8IYzwTCjIdKI7YxgmG9QFuPF5FUx_PsRQejTC4b-jTdL9EcvnWKAGA0JCVgphn8ICgSSQ6-lujGcRqLegpTE-T0B5wiUkBfKAz9dbnQrQcybfcDRC61FOvaemxFR_ymCMscbjq1AD',
              x: undefined,
              y: undefined,
              height: 32,
              width: 32,
              excavate: true,
            }}
          />
        </div>

        {/* Link URL Display */}
        <div className="w-full px-4 py-2 bg-surface-container rounded-lg font-code-sm text-code-sm text-brand-violet mb-6 select-all truncate">
          {url}
        </div>

        {/* Actions */}
        <div className="flex items-center gap-3 w-full">
          <button
            onClick={handleCopy}
            type="button"
            className="flex-1 py-2.5 px-4 rounded-xl bg-surface-card-subtle hover:bg-surface-container font-label-lg text-label-lg text-text-primary transition-colors flex items-center justify-center gap-2 border border-border-subtle"
          >
            <span className="material-symbols-outlined text-[18px]">content_copy</span>
            <span>Sao chép Link</span>
          </button>
          <button
            onClick={handleDownloadPng}
            type="button"
            className="flex-1 py-2.5 px-4 rounded-xl text-on-primary font-label-lg text-label-lg hover:brightness-105 active:scale-98 transition-all flex items-center justify-center gap-2 shadow-md bg-brand-violet"
          >
            <span className="material-symbols-outlined text-[18px]">download</span>
            <span>Tải mã PNG</span>
          </button>
        </div>
      </div>
    </div>
  );
};

export default QrModal;
