import React, { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Navbar = ({ onQuickShorten }) => {
  const { user, isAuthenticated, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const [isUserMenuOpen, setIsUserMenuOpen] = useState(false);

  const isActive = (path) => location.pathname === path;

  const handleQuickAction = () => {
    if (onQuickShorten) {
      onQuickShorten();
    } else if (location.pathname !== '/') {
      navigate('/');
    } else {
      const input = document.getElementById('originalUrlInput');
      if (input) {
        input.focus();
        input.scrollIntoView({ behavior: 'smooth', block: 'center' });
      }
    }
  };

  return (
    <header className="fixed top-0 inset-x-0 z-40 bg-surface-card/85 backdrop-blur-xl border-b border-border-subtle shadow-[0_1px_8px_rgba(0,0,0,0.04)]">
      <div className="h-16 max-w-7xl mx-auto px-4 sm:px-6 flex items-center justify-between gap-4">
        {/* Logo */}
        <div className="flex items-center gap-4 shrink-0">
          <Link to="/" className="flex items-center gap-2 group">
            <div className="w-8 h-8 rounded-lg bg-gradient-to-tr from-primary to-brand-violet flex items-center justify-center text-white shadow-sm group-hover:scale-105 transition-transform">
              <span className="material-symbols-outlined text-[20px]">link</span>
            </div>
            <span className="font-display text-headline-sm text-text-primary tracking-tight group-hover:text-primary transition-colors">
              SnapLink
            </span>
          </Link>
        </div>

        {/* Navigation Links */}
        <nav className="hidden lg:flex items-center gap-1 p-1 rounded-xl">
          <Link
            to="/"
            className={`px-4 py-1.5 rounded-lg transition-colors font-label-lg text-label-lg ${
              isActive('/')
                ? 'text-primary font-semibold bg-surface-container-low'
                : 'text-text-secondary hover:text-text-primary hover:bg-surface-container-high'
            }`}
          >
            Trang Chủ
          </Link>
          <Link
            to="/dashboard"
            className={`px-4 py-1.5 rounded-lg transition-colors font-label-lg text-label-lg ${
              isActive('/dashboard')
                ? 'text-primary font-semibold bg-surface-container-low'
                : 'text-text-secondary hover:text-text-primary hover:bg-surface-container-high'
            }`}
          >
            Dashboard
          </Link>
          <Link
            to="/dashboard"
            className="px-4 py-1.5 rounded-lg font-label-lg text-label-lg text-text-secondary hover:text-text-primary hover:bg-surface-container-high transition-colors"
          >
            Thống Kê
          </Link>
          <a
            href="https://github.com"
            target="_blank"
            rel="noopener noreferrer"
            className="px-4 py-1.5 rounded-lg font-label-lg text-label-lg text-text-secondary hover:text-text-primary hover:bg-surface-container-high transition-colors"
          >
            Tài Liệu API
          </a>
          <a
            href="#pricing"
            className="px-4 py-1.5 rounded-lg font-label-lg text-label-lg text-text-secondary hover:text-text-primary hover:bg-surface-container-high transition-colors"
          >
            Bảng Giá
          </a>
        </nav>

        {/* Right Action Hub */}
        <div className="flex items-center gap-3">
          {/* Quick Shorten CTA */}
          <button
            type="button"
            onClick={handleQuickAction}
            className="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl text-on-primary font-label-lg text-label-lg shadow-[0_4px_14px_0_rgba(124,58,237,0.35)] hover:brightness-105 active:scale-98 transition-all shrink-0 bg-brand-violet cursor-pointer"
          >
            <span className="material-symbols-outlined text-[18px]">add_link</span>
            <span className="hidden sm:inline">+ Rút gọn nhanh</span>
          </button>

          {/* Telemetry Status Notification */}
          <button
            type="button"
            aria-label="Thông báo"
            className="relative p-2 rounded-xl text-text-secondary hover:text-text-primary hover:bg-surface-card-subtle transition-colors border border-border-subtle"
          >
            <span className="material-symbols-outlined text-[20px]">notifications</span>
            <span className="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-status-emerald ring-2 ring-surface-card"></span>
          </button>

          {/* User Profile or Auth Buttons */}
          {isAuthenticated ? (
            <div className="relative">
              <button
                type="button"
                onClick={() => setIsUserMenuOpen(!isUserMenuOpen)}
                className="flex items-center gap-2 p-1.5 rounded-xl hover:bg-surface-card-subtle transition-colors border border-border-subtle group"
              >
                <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-primary to-brand-violet text-white flex items-center justify-center font-bold text-sm">
                  {(user?.fullName || user?.email || 'U')[0].toUpperCase()}
                </div>
                <div className="hidden md:flex flex-col text-left">
                  <span className="font-label-md text-label-md text-text-primary group-hover:text-primary transition-colors font-medium truncate max-w-[120px]">
                    {user?.fullName || user?.email?.split('@')[0]}
                  </span>
                </div>
                <span className="material-symbols-outlined text-text-muted text-[18px]">
                  expand_more
                </span>
              </button>

              {/* Dropdown Menu */}
              {isUserMenuOpen && (
                <div className="absolute right-0 mt-2 w-48 bg-surface-card rounded-xl shadow-2xl border border-border-subtle py-1.5 z-50 animate-in fade-in zoom-in-95 duration-150">
                  <div className="px-3 py-2 border-b border-border-subtle">
                    <p className="text-xs text-text-muted">Đăng nhập với</p>
                    <p className="font-label-md text-label-md text-text-primary truncate">{user?.email}</p>
                  </div>
                  <Link
                    to="/dashboard"
                    onClick={() => setIsUserMenuOpen(false)}
                    className="flex items-center gap-2 px-3 py-2 text-sm text-text-primary hover:bg-surface-container-low transition-colors"
                  >
                    <span className="material-symbols-outlined text-[18px] text-brand-violet">dashboard</span>
                    <span>Dashboard</span>
                  </Link>
                  <button
                    type="button"
                    onClick={() => {
                      setIsUserMenuOpen(false);
                      logout();
                      navigate('/login');
                    }}
                    className="w-full text-left flex items-center gap-2 px-3 py-2 text-sm text-error hover:bg-error-container/30 transition-colors"
                  >
                    <span className="material-symbols-outlined text-[18px]">logout</span>
                    <span>Đăng xuất</span>
                  </button>
                </div>
              )}
            </div>
          ) : (
            <div className="flex items-center gap-2">
              <Link
                to="/login"
                className="px-3.5 py-2 rounded-xl text-text-primary hover:text-brand-violet hover:bg-surface-container-low font-label-lg text-label-lg transition-colors"
              >
                Đăng Nhập
              </Link>
              <Link
                to="/register"
                className="hidden sm:inline-flex px-3.5 py-2 rounded-xl bg-surface-card-subtle hover:bg-surface-container text-text-primary font-label-lg text-label-lg transition-colors border border-border-subtle shadow-sm"
              >
                Đăng Ký
              </Link>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};

export default Navbar;
