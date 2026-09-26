import React from 'react';

const Footer = () => {
  return (
    <footer className="w-full bg-surface-card/90 backdrop-blur-md border-t border-border-subtle shadow-[0_1px_8px_rgba(0,0,0,0.04)] mt-16">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 py-6 flex flex-col md:flex-row items-center justify-between gap-4">
        <div className="flex items-center gap-2">
          <span className="font-body-sm text-body-sm text-text-secondary">
            © 2026 SnapLink SaaS Platform. All rights reserved.
          </span>
        </div>
        <div className="flex flex-wrap items-center gap-4">
          <a
            href="#terms"
            className="font-label-md text-label-md text-text-secondary hover:text-on-surface hover:bg-surface-container-high px-2 py-1 rounded transition-colors"
          >
            Điều khoản
          </a>
          <a
            href="#privacy"
            className="font-label-md text-label-md text-text-secondary hover:text-on-surface hover:bg-surface-container-high px-2 py-1 rounded transition-colors"
          >
            Bảo mật
          </a>
          <a
            href="#support"
            className="font-label-md text-label-md text-text-secondary hover:text-on-surface hover:bg-surface-container-high px-2 py-1 rounded transition-colors"
          >
            Hỗ trợ
          </a>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
