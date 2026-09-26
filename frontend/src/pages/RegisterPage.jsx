import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const RegisterPage = () => {
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  const { register } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMessage('');
    setIsLoading(true);

    try {
      await register(email.trim(), password, fullName.trim());
      navigate('/dashboard', { replace: true });
    } catch (err) {
      const msg =
        err.response?.data?.message ||
        err.response?.data?.error ||
        'Đăng ký thất bại: Email có thể đã tồn tại hoặc dữ liệu không hợp lệ.';
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
        {/* Register Glassmorphism Card */}
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
                Tạo tài khoản mới
              </h1>
              <p className="font-body-sm text-body-sm text-text-secondary max-w-xs mx-auto">
                Bắt đầu rút gọn link chuyên nghiệp và theo dõi telemetry chi tiết
              </p>
            </div>
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

          {/* Register Form */}
          <form className="space-y-4" onSubmit={handleSubmit}>
            {/* Full Name Field */}
            <div className="space-y-1.5">
              <label className="block font-label-lg text-label-lg text-text-primary" htmlFor="fullName">
                Họ và tên
              </label>
              <div className="relative flex items-center">
                <span className="material-symbols-outlined absolute left-3.5 text-text-muted pointer-events-none text-xl">
                  person
                </span>
                <input
                  id="fullName"
                  type="text"
                  required
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                  placeholder="Nguyễn Văn A"
                  className="w-full pl-10 pr-4 py-2.5 bg-surface-container-low focus:bg-surface-card text-text-primary rounded-xl font-body-md text-body-md transition-all duration-200 border border-border-subtle focus:border-brand-violet outline-none placeholder:text-text-muted"
                />
              </div>
            </div>

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
              <label className="block font-label-lg text-label-lg text-text-primary" htmlFor="password">
                Mật khẩu (tối thiểu 6 ký tự)
              </label>
              <div className="relative flex items-center">
                <span className="material-symbols-outlined absolute left-3.5 text-text-muted pointer-events-none text-xl">
                  lock
                </span>
                <input
                  id="password"
                  type={showPassword ? 'text' : 'password'}
                  required
                  minLength={6}
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
                  <span>Đăng Ký Miễn Phí</span>
                  <span className="material-symbols-outlined text-lg transition-transform group-hover:translate-x-1">
                    arrow_forward
                  </span>
                </>
              )}
            </button>
          </form>

          {/* Footer Login Hook */}
          <div className="text-center pt-2">
            <p className="font-body-sm text-body-sm text-text-secondary">
              Đã có tài khoản?{' '}
              <Link
                to="/login"
                className="font-semibold text-brand-indigo hover:text-brand-violet underline underline-offset-4 decoration-brand-indigo/30 transition-colors"
              >
                Đăng nhập ngay
              </Link>
            </p>
          </div>
        </div>
      </main>
    </div>
  );
};

export default RegisterPage;
