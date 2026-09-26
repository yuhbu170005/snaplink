import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import ResultCard from '../components/ResultCard';
import QrModal from '../components/QrModal';
import Toast from '../components/Toast';
import { urlApi } from '../api/urlApi';

const HomePage = () => {
  const [originalUrl, setOriginalUrl] = useState('');
  const [customAlias, setCustomAlias] = useState('');
  const [expiresAt, setExpiresAt] = useState('');
  const [isAdvancedOpen, setIsAdvancedOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [createdUrl, setCreatedUrl] = useState(null);
  const [formError, setFormError] = useState('');

  // QR Modal State
  const [qrModalData, setQrModalData] = useState({ isOpen: false, url: '', code: '' });

  // Toast State
  const [toast, setToast] = useState({ show: false, message: '', title: 'Thành công', type: 'success' });

  const triggerToast = (message, title = 'Thành công', type = 'success') => {
    setToast({ show: true, message, title, type });
    setTimeout(() => {
      setToast((prev) => ({ ...prev, show: false }));
    }, 2800);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormError('');

    if (!originalUrl) {
      setFormError('Vui lòng nhập đường dẫn gốc cần rút gọn.');
      return;
    }

    try {
      setIsLoading(true);
      const payload = {
        originalUrl: originalUrl.trim(),
        customAlias: customAlias.trim() || undefined,
        expiresAt: expiresAt ? new Date(expiresAt).toISOString() : undefined,
      };

      const result = await urlApi.createUrl(payload);
      setCreatedUrl(result);
      triggerToast('Đã rút gọn liên kết thành công!', '🎉 Hoàn tất');
      
      // Auto-scroll to result card
      setTimeout(() => {
        const resultElement = document.getElementById('resultCard');
        if (resultElement) {
          resultElement.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        }
      }, 100);
    } catch (err) {
      const errorMsg =
        err.response?.data?.message ||
        err.response?.data?.error ||
        'Không thể rút gọn link. Vui lòng kiểm tra lại URL hoặc alias của bạn.';
      setFormError(errorMsg);
      triggerToast(errorMsg, 'Lỗi', 'error');
    } finally {
      setIsLoading(false);
    }
  };

  const handleOpenQr = (url, code) => {
    setQrModalData({ isOpen: true, url, code });
  };

  return (
    <div className="min-h-screen flex flex-col relative overflow-x-hidden bg-canvas-bg">
      {/* Background Image Layer (Ảnh nền) */}
      <div
        className="fixed inset-0 pointer-events-none z-0 bg-cover bg-center bg-no-repeat opacity-50"
        style={{ backgroundImage: "url('/background.jpeg')" }}
      />
      {/* Soft overlay gradient to ensure high readability */}
      <div className="fixed inset-0 pointer-events-none z-0 bg-gradient-to-b from-canvas-bg/20 via-canvas-bg/50 to-canvas-bg/90" />
      <div className="fixed inset-0 pointer-events-none z-0 bg-dot-matrix opacity-30" />

      <Navbar />

      <main className="flex-1 w-full pt-20 pb-12 relative z-10">
        {/* Ambient Halo Glow */}
        <div className="absolute top-0 left-1/2 -translate-x-1/2 w-full max-w-7xl h-96 ambient-glow pointer-events-none -z-10" />

        {/* SECTION 1: HERO & URL ENGINE */}
        <section className="relative w-full max-w-7xl mx-auto px-4 sm:px-6 pt-8 pb-12 flex flex-col items-center text-center">


          {/* Hero Title */}
          <h1 className="font-display text-4xl sm:text-5xl md:text-6xl text-text-primary font-bold max-w-4xl tracking-tight mb-4 leading-tight">
            Rút Gọn Link Thông Minh,{' '}
            <span className="text-gradient">Theo Dõi Toàn Diện</span>
          </h1>

          {/* Subtitle */}
          <p className="font-body-lg text-body-lg text-text-secondary max-w-2xl mb-8 leading-relaxed">
            Biến những liên kết dài dòng thành đường dẫn ngắn gọn, an toàn và phân tích lưu lượng truy cập chuyên sâu chỉ trong 1 giây.
          </p>

          {/* URL Shortener Engine Card */}
          <div className="w-full max-w-4xl bg-surface-card rounded-2xl shadow-xl p-4 sm:p-6 md:p-8 relative text-left border border-border-subtle">
            <form className="space-y-4" onSubmit={handleSubmit}>
              {/* Main Input Capsule */}
              <div className="relative flex flex-col sm:flex-row items-stretch sm:items-center bg-surface-card-subtle rounded-2xl p-2 border border-border-subtle focus-within:border-brand-violet focus-within:ring-2 focus-within:ring-brand-violet/20 transition-all">
                <div className="flex items-center gap-3 px-3 flex-1 min-w-0">
                  <span className="material-symbols-outlined text-text-muted text-[24px] shrink-0">
                    link
                  </span>
                  <input
                    type="url"
                    required
                    id="originalUrlInput"
                    value={originalUrl}
                    onChange={(e) => setOriginalUrl(e.target.value)}
                    placeholder="Dán đường dẫn cần rút gọn tại đây (https://your-very-long-url-here.com/...)"
                    className="w-full bg-transparent py-2.5 text-text-primary font-body-md text-body-md placeholder:text-text-muted focus:outline-none"
                  />
                </div>
                <button
                  type="submit"
                  disabled={isLoading}
                  className="mt-2 sm:mt-0 shrink-0 px-6 py-3 rounded-xl hover:brightness-105 active:scale-98 text-on-primary font-label-lg text-label-lg flex items-center justify-center gap-2 shadow-md transition-all bg-brand-violet cursor-pointer disabled:opacity-75"
                >
                  {isLoading ? (
                    <>
                      <svg className="animate-spin h-5 w-5 text-white" viewBox="0 0 24 24" fill="none">
                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                      </svg>
                      <span>Đang xử lý...</span>
                    </>
                  ) : (
                    <>
                      <span className="material-symbols-outlined text-[18px]">auto_fix_high</span>
                      <span>Rút gọn ngay</span>
                    </>
                  )}
                </button>
              </div>

              {/* Error Box */}
              {formError && (
                <div className="flex items-center gap-2 p-3 bg-error-container/40 border border-error/20 text-error rounded-xl font-body-sm text-body-sm animate-in fade-in duration-150">
                  <span className="material-symbols-outlined text-lg shrink-0">error</span>
                  <span>{formError}</span>
                </div>
              )}

              {/* Accordion: Tuỳ chọn nâng cao */}
              <div className="rounded-xl bg-surface-container-low p-4 border border-border-subtle">
                <div
                  onClick={() => setIsAdvancedOpen(!isAdvancedOpen)}
                  className="flex items-center justify-between cursor-pointer select-none"
                >
                  <div className="flex items-center gap-2">
                    <span className="material-symbols-outlined text-brand-violet text-[20px]">
                      tune
                    </span>
                    <span className="font-label-lg text-label-lg text-text-primary">
                      Tuỳ chọn nâng cao
                    </span>
                    <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-semibold bg-brand-violet/10 text-brand-violet">
                      Custom Alias & Hạn dùng
                    </span>
                  </div>
                  <span className="material-symbols-outlined text-[20px] text-text-secondary transition-transform duration-200">
                    {isAdvancedOpen ? 'expand_less' : 'expand_more'}
                  </span>
                </div>

                {/* Collapsible content */}
                {isAdvancedOpen && (
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pt-4 mt-2 border-t border-border-subtle/50 animate-in fade-in duration-150">
                    {/* Custom Alias */}
                    <div className="space-y-1.5">
                      <label className="block font-label-md text-label-md text-text-secondary">
                        Tên đại diện tuỳ chỉnh (Custom Alias)
                      </label>
                      <div className="flex items-center bg-surface-card rounded-xl overflow-hidden shadow-sm border border-border-subtle">
                        <span className="px-3 py-2 bg-surface-card-subtle font-code-sm text-code-sm text-text-muted select-none border-r border-border-subtle">
                          snaplink.io/
                        </span>
                        <input
                          type="text"
                          value={customAlias}
                          onChange={(e) => setCustomAlias(e.target.value)}
                          placeholder="alias-cua-ban"
                          className="w-full px-3 py-2 font-code-sm text-code-sm text-text-primary focus:outline-none bg-transparent"
                        />
                      </div>
                    </div>

                    {/* Expiration Date */}
                    <div className="space-y-1.5">
                      <label className="block font-label-md text-label-md text-text-secondary">
                        Hạn sử dụng liên kết
                      </label>
                      <div className="relative flex items-center bg-surface-card rounded-xl shadow-sm border border-border-subtle">
                        <span className="material-symbols-outlined absolute left-3 text-text-muted text-[18px]">
                          calendar_today
                        </span>
                        <input
                          type="date"
                          value={expiresAt}
                          onChange={(e) => setExpiresAt(e.target.value)}
                          className="w-full pl-10 pr-3 py-2 font-body-sm text-body-sm text-text-primary focus:outline-none bg-transparent rounded-xl"
                        />
                      </div>
                    </div>
                  </div>
                )}
              </div>
            </form>
          </div>

          {/* Result Card Component */}
          {createdUrl && (
            <ResultCard
              urlData={createdUrl}
              onOpenQr={handleOpenQr}
              onShowToast={(msg) => triggerToast(msg, 'Đã sao chép')}
            />
          )}
        </section>

        

        {/* SECTION 3: BENTO GRID FEATURES */}
        <section className="w-full max-w-7xl mx-auto px-4 sm:px-6 py-12">
          <div className="text-center max-w-2xl mx-auto mb-12">
            <span className="font-label-md text-label-md uppercase tracking-wider text-brand-violet font-semibold">
              Khả năng vượt trội
            </span>
            <h2 className="font-display text-3xl sm:text-4xl text-text-primary font-bold mt-2 mb-3">
              Được xây dựng cho tốc độ, thiết kế cho sự chuẩn xác
            </h2>
            <p className="font-body-md text-body-md text-text-secondary">
              Mọi tính năng đều hướng tới trải nghiệm tối giản nhưng sở hữu sức mạnh viễn thông chuyên sâu dành cho Marketing và Nhà phát triển.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {/* Feature 1 */}
            <div className="bg-surface-card rounded-2xl p-6 sm:p-8 shadow-sm hover:shadow-xl transition-all duration-300 flex flex-col justify-between border border-border-subtle group">
              <div>
                <div className="w-12 h-12 rounded-xl bg-surface-container flex items-center justify-center text-primary group-hover:scale-105 transition-transform mb-4">
                  <span className="material-symbols-outlined text-[24px]">query_stats</span>
                </div>
                <h3 className="font-display text-xl font-bold text-text-primary mb-2">
                  Phân tích thời gian thực
                </h3>
                <p className="font-body-md text-body-md text-text-secondary mb-6 leading-relaxed">
                  Nắm bắt tức thì lưu lượng truy cập theo quốc gia, hệ điều hành (iOS, Android, macOS), nguồn giới thiệu và tỷ lệ chuyển đổi.
                </p>
              </div>
              <div className="p-4 bg-surface-card-subtle rounded-xl space-y-2 border border-border-subtle">
                <div className="flex items-center justify-between font-label-md text-label-md text-text-secondary">
                  <span>Clicks hôm nay</span>
                  <span className="font-semibold text-status-emerald flex items-center gap-0.5">
                    <span className="material-symbols-outlined text-[14px]">arrow_upward</span> +24.8%
                  </span>
                </div>
                <div className="w-full h-1.5 bg-surface-container rounded-full overflow-hidden">
                  <div className="bg-primary h-full rounded-full" style={{ width: '75%' }}></div>
                </div>
              </div>
            </div>

            {/* Feature 2 */}
            <div className="bg-surface-card rounded-2xl p-6 sm:p-8 shadow-sm hover:shadow-xl transition-all duration-300 flex flex-col justify-between border border-border-subtle group">
              <div>
                <div className="w-12 h-12 rounded-xl bg-purple-50 flex items-center justify-center text-brand-violet group-hover:scale-105 transition-transform mb-4">
                  <span className="material-symbols-outlined text-[24px]">qr_code_scanner</span>
                </div>
                <h3 className="font-display text-xl font-bold text-text-primary mb-2">
                  Mã QR động thương hiệu
                </h3>
                <p className="font-body-md text-body-md text-text-secondary mb-6 leading-relaxed">
                  Tự động sinh mã QR chất lượng cao, hỗ trợ tải ảnh PNG chuẩn vector in ấn và quét trực tiếp từ mọi thiết bị di động.
                </p>
              </div>
              <div className="p-4 bg-surface-card-subtle rounded-xl flex items-center justify-between border border-border-subtle">
                <div className="flex items-center gap-2">
                  <span className="w-5 h-5 rounded-full bg-brand-violet"></span>
                  <span className="w-5 h-5 rounded-full bg-primary"></span>
                  <span className="w-5 h-5 rounded-full bg-status-emerald"></span>
                  <span className="font-label-md text-label-md text-text-muted pl-1">Palette</span>
                </div>
                <span className="inline-flex items-center gap-1 font-label-md text-label-md text-brand-violet font-semibold">
                  <span className="material-symbols-outlined text-[16px]">verified</span> Vector HD
                </span>
              </div>
            </div>

            {/* Feature 3 */}
            <div className="bg-surface-card rounded-2xl p-6 sm:p-8 shadow-sm hover:shadow-xl transition-all duration-300 flex flex-col justify-between border border-border-subtle group">
              <div>
                <div className="w-12 h-12 rounded-xl bg-emerald-50 flex items-center justify-center text-status-emerald group-hover:scale-105 transition-transform mb-4">
                  <span className="material-symbols-outlined text-[24px]">verified_user</span>
                </div>
                <h3 className="font-display text-xl font-bold text-text-primary mb-2">
                  Bảo mật chống phishing
                </h3>
                <p className="font-body-md text-body-md text-text-secondary mb-6 leading-relaxed">
                  Hệ thống kiểm tra scheme an toàn (chặn javascript:, data:), ngăn chặn giả mạo thương hiệu và bảo vệ người dùng cuối tuyệt đối.
                </p>
              </div>
              <div className="p-4 bg-surface-card-subtle rounded-xl flex items-center justify-between border border-border-subtle">
                <div className="flex items-center gap-2">
                  <span className="material-symbols-outlined text-status-emerald text-[20px]">security</span>
                  <span className="font-label-md text-label-md text-text-primary font-medium">Safe Scheme Guard</span>
                </div>
                <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-100 text-status-emerald">
                  100% An toàn
                </span>
              </div>
            </div>
          </div>
        </section>

      </main>

      {/* Dynamic QR Modal */}
      <QrModal
        isOpen={qrModalData.isOpen}
        onClose={() => setQrModalData({ ...qrModalData, isOpen: false })}
        url={qrModalData.url}
        shortCode={qrModalData.code}
        onCopySuccess={() => triggerToast('Đã sao chép liên kết!', 'Thành công')}
      />

      {/* Interactive Toast Notification */}
      <Toast
        show={toast.show}
        message={toast.message}
        title={toast.title}
        type={toast.type}
      />

      <Footer />
    </div>
  );
};

export default HomePage;
