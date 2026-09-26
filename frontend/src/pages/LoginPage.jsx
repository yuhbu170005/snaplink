import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const LoginPage = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [rememberMe, setRememberMe] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const from = location.state?.from?.pathname || '/dashboard';

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMessage('');
    setIsLoading(true);

    try {
      await login(email.trim(), password);
      navigate(from, { replace: true });
    } catch (err) {
      const msg =
        err.response?.data?.message ||
        err.response?.data?.error ||
        'Đăng nhập thất bại: Email hoặc mật khẩu không chính xác.';
      setErrorMessage(msg);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-canvas-bg bg-dot-matrix relative flex flex-col justify-center items-center p-4">
      {/* Ambient Center Glow */}
      <div className="absolute inset-0 pointer-events-none -z-10 flex items-center justify-center overflow-hidden">
        <div className="w-[500px] h-[500px] bg-brand-violet/10 rounded-full blur-3xl"></div>
      </div>

      <main className="w-full max-w-md">
        {/* Login Glassmorphism Card */}
        <div className="relative backdrop-blur-xl rounded-2xl shadow-xl p-6 sm:p-8 space-y-6 bg-surface-card border border-border-subtle">
          {/* Top Brand Header */}
          <div className="flex flex-col items-center text-center space-y-3">
            <Link
              to="/"
              className="inline-flex items-center gap-2 group transition-transform hover:scale-105"
            >
              <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-primary to-brand-violet flex items-center justify-center text-white shadow-md">
                <span className="material-symbols-outlined text-[24px]">link</span>
              </div>
            </Link>
            <div className="space-y-1">
              <h1 className="font-display text-2xl font-bold text-text-primary tracking-tight">
                Chào mừng trở lại
              </h1>
            </div>
          </div>

          {/* Quick Social Auth Buttons */}
          <div className="grid grid-cols-2 gap-3">
            <button
              type="button"
              onClick={() => alert('Đăng nhập với Google sắp ra mắt')}
              className="group flex items-center justify-center gap-2 py-2.5 px-4 rounded-xl bg-surface-card-subtle hover:bg-surface-container text-text-primary text-label-lg font-label-lg transition-all duration-200 hover:-translate-y-0.5 active:scale-95 border border-border-subtle shadow-sm cursor-pointer"
            >
              <svg className="w-4 h-4 shrink-0 transition-transform group-hover:scale-110" viewBox="0 0 24 24">
                <path
                  d="M23.745 12.27c0-.7-.06-1.4-.19-2.07H12v4.51h6.6c-.29 1.52-1.14 2.8-2.4 3.68v3.05h3.88c2.27-2.09 3.66-5.17 3.66-9.17z"
                  fill="#4285F4"
                ></path>
                <path
                  d="M12 24c3.24 0 5.95-1.08 7.93-2.91l-3.88-3.05c-1.08.72-2.45 1.16-4.05 1.16-3.12 0-5.77-2.1-6.72-4.94H1.27v3.15C3.26 21.36 7.33 24 12 24z"
                  fill="#34A853"
                ></path>
                <path
                  d="M5.28 14.26a7.12 7.12 0 0 1 0-4.52V6.59H1.27a11.97 11.97 0 0 0 0 10.82l4.01-3.15z"
                  fill="#FBBC05"
                ></path>
                <path
                  d="M12 4.77c1.77 0 3.35.61 4.6 1.8l3.42-3.42C17.95 1.19 15.24 0 12 0 7.33 0 3.26 2.64 1.27 6.59l4.01 3.15c.95-2.84 3.6-4.97 6.72-4.97z"
                  fill="#EA4335"
                ></path>
              </svg>
              <span>Google</span>
            </button>
            <button
              type="button"
              onClick={() => alert('Đăng nhập với GitHub sắp ra mắt')}
              className="group flex items-center justify-center gap-2 py-2.5 px-4 rounded-xl bg-surface-card-subtle hover:bg-surface-container text-text-primary text-label-lg font-label-lg transition-all duration-200 hover:-translate-y-0.5 active:scale-95 border border-border-subtle shadow-sm cursor-pointer"
            >
              <svg
                className="w-4 h-4 shrink-0 fill-current text-text-primary transition-transform group-hover:scale-110"
                viewBox="0 0 24 24"
              >
                <path
                  clipRule="evenodd"
                  d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.53 1.032 1.53 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z"
                  fillRule="evenodd"
                ></path>
              </svg>
              <span>GitHub</span>
            </button>
          </div>

          {/* Divider */}
          <div className="relative flex items-center justify-center">
            <div className="w-full h-px bg-surface-container-high"></div>
            <span className="absolute bg-surface-card px-3 text-text-muted font-label-md text-label-md uppercase tracking-wider">
              Hoặc đăng nhập bằng email
            </span>
          </div>

          {/* Error Alert Box */}
          {errorMessage && (
            <div className="flex items-start gap-2.5 p-3 rounded-xl bg-error-container/40 border border-error/20 text-error animate-in fade-in duration-150">
              <span className="material-symbols-outlined text-lg shrink-0 mt-0.5">error</span>
              <div className="font-body-sm text-body-sm text-error">
                <span className="font-semibold">Lỗi:</span> {errorMessage}
              </div>
            </div>
          )}

          {/* Login Form */}
          <form className="space-y-4" onSubmit={handleSubmit}>
            {/* Email Field */}
            <div className="space-y-1.5">
              <label className="block font-label-lg text-label-lg text-text-primary" htmlFor="email">
                Email
              </label>
              <div className="relative flex items-center">
                <span className="material-symbols-outlined absolute left-3.5 text-text-muted pointer-events-none text-xl">
                  mail
                </span>
                <input
                  id="email"
                  type="email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="name@domain.com"
                  className="w-full pl-10 pr-4 py-2.5 bg-surface-container-low focus:bg-surface-card text-text-primary rounded-xl font-body-md text-body-md transition-all duration-200 border border-border-subtle focus:border-brand-violet outline-none placeholder:text-text-muted"
                />
              </div>
            </div>

            {/* Password Field */}
            <div className="space-y-1.5">
              <div className="flex items-center justify-between">
                <label className="block font-label-lg text-label-lg text-text-primary" htmlFor="password">
                  Mật khẩu
                </label>
                <a href="#forgot" className="font-label-md text-label-md text-brand-indigo hover:text-brand-violet transition-colors">
                  Quên mật khẩu?
                </a>
              </div>
              <div className="relative flex items-center">
                <span className="material-symbols-outlined absolute left-3.5 text-text-muted pointer-events-none text-xl">
                  lock
                </span>
                <input
                  id="password"
                  type={showPassword ? 'text' : 'password'}
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  className="w-full pl-10 pr-11 py-2.5 bg-surface-container-low focus:bg-surface-card text-text-primary rounded-xl font-body-md text-body-md transition-all duration-200 border border-border-subtle focus:border-brand-violet outline-none placeholder:text-text-muted"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  aria-label="Toggle password visibility"
                  className="absolute right-3 p-1 rounded-lg text-text-muted hover:text-text-primary transition-colors flex items-center justify-center focus:outline-none"
                >
                  <span className="material-symbols-outlined text-xl">
                    {showPassword ? 'visibility_off' : 'visibility'}
                  </span>
                </button>
              </div>
            </div>

            {/* Remember Me */}
            <div className="flex items-center justify-between pt-1">
              <label className="flex items-center gap-2 cursor-pointer select-none">
                <input
                  type="checkbox"
                  checked={rememberMe}
                  onChange={(e) => setRememberMe(e.target.checked)}
                  className="w-4 h-4 rounded text-brand-indigo accent-brand-indigo cursor-pointer transition"
                />
                <span className="font-body-sm text-body-sm text-text-secondary">Ghi nhớ đăng nhập</span>
              </label>
            </div>

            {/* Submit Button */}
            <button
              type="submit"
              disabled={isLoading}
              className="w-full relative group overflow-hidden py-3 px-5 rounded-xl text-on-primary font-label-lg text-label-lg transition-all duration-200 hover:-translate-y-0.5 active:scale-[0.99] shadow-lg shadow-brand-indigo/25 flex items-center justify-center gap-2 bg-brand-violet cursor-pointer disabled:opacity-75"
            >
              {isLoading ? (
                <svg className="animate-spin h-5 w-5 text-white" viewBox="0 0 24 24" fill="none">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
              ) : (
                <>
                  <span>Đăng Nhập Ngay</span>
                  <span className="material-symbols-outlined text-lg transition-transform group-hover:translate-x-1">
                    arrow_forward
                  </span>
                </>
              )}
            </button>
          </form>

          {/* Footer Registration Hook */}
          <div className="text-center pt-2">
            <p className="font-body-sm text-body-sm text-text-secondary">
              Chưa có tài khoản?{' '}
              <Link
                to="/register"
                className="font-semibold text-brand-indigo hover:text-brand-violet underline underline-offset-4 decoration-brand-indigo/30 transition-colors"
              >
                Đăng ký miễn phí ngay
              </Link>
            </p>
          </div>

        </div>
      </main>
    </div>
  );
};

export default LoginPage;
