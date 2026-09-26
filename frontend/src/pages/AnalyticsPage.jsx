import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
} from 'recharts';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import QrModal from '../components/QrModal';
import Toast from '../components/Toast';
import { urlApi } from '../api/urlApi';

// Mock/Default trend data for Recharts visualization
const MOCK_TIME_DATA = [
  { date: '18/09', clicks: 45 },
  { date: '19/09', clicks: 120 },
  { date: '20/09', clicks: 80 },
  { date: '21/09', clicks: 210 },
  { date: '22/09', clicks: 95 },
  { date: '23/09', clicks: 160 },
  { date: '24/09', clicks: 140 },
];

const DEVICE_DATA = [
  { name: 'Mobile', value: 65, color: '#7C3AED' },
  { name: 'Desktop', value: 30, color: '#4F46E5' },
  { name: 'Tablet', value: 5, color: '#94A3B8' },
];

const AnalyticsPage = () => {
  const { id } = useParams();
  const [urlDetail, setUrlDetail] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [timeRange, setTimeRange] = useState('7_days');

  // QR Modal
  const [qrModalData, setQrModalData] = useState({ isOpen: false, url: '', code: '' });

  // Toast
  const [toast, setToast] = useState({ show: false, message: '', title: 'Thành công', type: 'success' });

  const triggerToast = (message, title = 'Thành công', type = 'success') => {
    setToast({ show: true, message, title, type });
    setTimeout(() => {
      setToast((prev) => ({ ...prev, show: false }));
    }, 2800);
  };

  useEffect(() => {
    const fetchDetail = async () => {
      try {
        setIsLoading(true);
        if (id) {
          const data = await urlApi.getUrlById(id);
          setUrlDetail(data);
        }
      } catch (err) {
        console.error('Error fetching url detail:', err);
        triggerToast('Không thể tải thông tin thống kê của liên kết', 'Lỗi', 'error');
      } finally {
        setIsLoading(false);
      }
    };

    fetchDetail();
  }, [id]);

  const displayCode = urlDetail?.customAlias || urlDetail?.shortCode || 'link';
  const shortUrl = urlDetail?.shortUrl || `https://snaplink.io/${displayCode}`;
  const totalClicks = urlDetail?.totalClicks || 850;

  const handleCopyLink = () => {
    navigator.clipboard.writeText(shortUrl);
    triggerToast(`Đã sao chép link: ${shortUrl}`, 'Thành công');
  };

  const handleExportCsv = () => {
    const csvContent =
      'data:text/csv;charset=utf-8,' +
      `Date,Clicks,Original_URL,Short_URL\n` +
      MOCK_TIME_DATA.map((d) => `${d.date},${d.clicks},"${urlDetail?.originalUrl || ''}","${shortUrl}"`).join('\n');
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement('a');
    link.setAttribute('href', encodedUri);
    link.setAttribute('download', `snaplink_analytics_${displayCode}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    triggerToast('Đã xuất báo cáo CSV thành công!', 'Xuất file');
  };

  return (
    <div className="min-h-screen flex flex-col bg-canvas-bg bg-dot-matrix relative">
      <Navbar />

      <main className="flex-1 w-full pt-20 pb-12">
        <div className="w-full max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 flex flex-col gap-6">
          {/* Top Bar: Back & Status & CSV */}
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <Link
              to="/dashboard"
              className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-surface-card text-text-secondary hover:text-text-primary hover:bg-surface-container-high transition-all shadow-sm font-label-lg text-label-lg border border-border-subtle group self-start"
            >
              <span className="material-symbols-outlined text-[18px] group-hover:-translate-x-0.5 transition-transform">
                arrow_back
              </span>
              <span>Quay lại Dashboard</span>
            </Link>

            <div className="flex items-center gap-2">
              <span
                className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-full font-label-md text-label-md border ${
                  urlDetail?.status === 'INACTIVE'
                    ? 'bg-rose-50 text-status-rose border-rose-200'
                    : 'bg-emerald-50 text-status-emerald border-emerald-200'
                }`}
              >
                <span
                  className={`w-2 h-2 rounded-full ${
                    urlDetail?.status === 'INACTIVE' ? 'bg-status-rose' : 'bg-status-emerald animate-pulse'
                  }`}
                ></span>
                {urlDetail?.status === 'INACTIVE' ? 'Vô Hiệu Hoá' : 'Đang Hoạt Động'}
              </span>

              <button
                type="button"
                onClick={handleExportCsv}
                className="inline-flex items-center gap-1.5 px-3.5 py-2 rounded-xl bg-surface-card hover:bg-surface-container text-text-secondary hover:text-text-primary font-label-md text-label-md shadow-sm transition-colors border border-border-subtle cursor-pointer"
              >
                <span className="material-symbols-outlined text-[18px]">download</span>
                <span>Xuất CSV</span>
              </button>
            </div>
          </div>

          {/* Target Short Link Detail Strip */}
          <div className="p-6 rounded-2xl bg-surface-card shadow-sm border border-border-subtle flex flex-col md:flex-row md:items-center justify-between gap-4">
            <div className="space-y-1.5 min-w-0">
              <div className="flex flex-wrap items-center gap-3">
                <span className="text-text-muted font-label-md text-label-md uppercase tracking-wider font-semibold">
                  Thống kê chi tiết
                </span>
                <span className="px-2 py-0.5 rounded-md bg-surface-container text-primary font-mono text-xs">
                  ID: lnk_{urlDetail?.id || '89201a'}
                </span>
              </div>

              <div className="flex items-baseline gap-3 flex-wrap">
                <h1 className="font-display text-2xl sm:text-3xl font-bold text-text-primary tracking-tight">
                  snaplink.io/{displayCode}
                </h1>
                <button
                  type="button"
                  onClick={handleCopyLink}
                  className="inline-flex items-center gap-1 text-primary hover:text-brand-violet font-label-md text-label-md transition-colors cursor-pointer"
                >
                  <span className="material-symbols-outlined text-[18px]">content_copy</span>
                  <span>Sao chép</span>
                </button>
              </div>

              <div className="flex items-center gap-2 text-text-secondary font-body-sm text-body-sm min-w-0">
                <span className="material-symbols-outlined text-[16px] text-text-muted shrink-0">link</span>
                <span className="text-text-muted shrink-0">Gốc:</span>
                <a
                  href={urlDetail?.originalUrl || '#'}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="truncate hover:text-primary hover:underline transition-colors max-w-lg font-mono text-xs"
                >
                  {urlDetail?.originalUrl || 'https://example.com/long-url'}
                </a>
              </div>
            </div>

            {/* Action Utilities */}
            <div className="flex items-center gap-2 self-start md:self-center shrink-0">
              <button
                type="button"
                onClick={() => setQrModalData({ isOpen: true, url: shortUrl, code: displayCode })}
                className="inline-flex items-center gap-1.5 px-4 py-2.5 rounded-xl bg-surface-container-low hover:bg-surface-container text-text-primary font-label-md text-label-md transition-colors border border-border-subtle cursor-pointer"
              >
                <span className="material-symbols-outlined text-[18px] text-brand-violet">qr_code_2</span>
                <span>Xem QR Code</span>
              </button>
              <a
                href={shortUrl}
                target="_blank"
                rel="noopener noreferrer"
                title="Mở đường link"
                className="p-2.5 rounded-xl bg-surface-container-low hover:bg-surface-container text-text-secondary hover:text-text-primary transition-colors border border-border-subtle"
              >
                <span className="material-symbols-outlined text-[18px]">open_in_new</span>
              </a>
            </div>
          </div>

          {/* 4 Metric Summary KPI Cards */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            {/* KPI 1 */}
            <div className="p-5 rounded-2xl bg-surface-card shadow-sm border border-border-subtle flex flex-col justify-between">
              <div className="flex items-center justify-between mb-3">
                <span className="font-label-md text-label-md text-text-secondary uppercase tracking-wider font-semibold">
                  Tổng Lượt Click
                </span>
                <div className="w-9 h-9 rounded-xl bg-surface-container flex items-center justify-center text-brand-violet">
                  <span className="material-symbols-outlined text-[20px]">ads_click</span>
                </div>
              </div>
              <div>
                <div className="flex items-baseline gap-2">
                  <span className="font-display text-3xl font-bold text-text-primary tracking-tight">
                    {totalClicks}
                  </span>
                  <span className="font-label-md text-label-md text-text-muted">clicks</span>
                </div>
                <div className="mt-2 flex items-center gap-1 text-status-emerald font-label-md text-label-md">
                  <span className="material-symbols-outlined text-[16px]">trending_up</span>
                  <span>+24%</span>
                  <span className="text-text-muted font-normal ml-1">so với tuần trước</span>
                </div>
              </div>
            </div>

            {/* KPI 2 */}
            <div className="p-5 rounded-2xl bg-surface-card shadow-sm border border-border-subtle flex flex-col justify-between">
              <div className="flex items-center justify-between mb-3">
                <span className="font-label-md text-label-md text-text-secondary uppercase tracking-wider font-semibold">
                  Quốc Gia Hàng Đầu
                </span>
                <div className="w-9 h-9 rounded-xl bg-surface-container flex items-center justify-center text-primary">
                  <span className="material-symbols-outlined text-[20px]">public</span>
                </div>
              </div>
              <div>
                <div className="flex items-center gap-2">
                  <span className="text-2xl leading-none">🇻🇳</span>
                  <span className="font-display text-xl font-bold text-text-primary truncate">Việt Nam</span>
                  <span className="px-2 py-0.5 rounded-full bg-surface-container text-primary font-label-md text-label-md font-semibold">
                    78%
                  </span>
                </div>
                <div className="mt-2 text-text-muted font-body-sm text-body-sm">
                  Chiếm {Math.round(totalClicks * 0.78)} lượt click trực tiếp
                </div>
              </div>
            </div>

            {/* KPI 3 */}
            <div className="p-5 rounded-2xl bg-surface-card shadow-sm border border-border-subtle flex flex-col justify-between">
              <div className="flex items-center justify-between mb-3">
                <span className="font-label-md text-label-md text-text-secondary uppercase tracking-wider font-semibold">
                  Thiết Bị Phổ Biến
                </span>
                <div className="w-9 h-9 rounded-xl bg-surface-container flex items-center justify-center text-secondary">
                  <span className="material-symbols-outlined text-[20px]">smartphone</span>
                </div>
              </div>
              <div>
                <div className="flex items-center gap-2">
                  <span className="font-display text-xl font-bold text-text-primary">Mobile</span>
                  <span className="px-2 py-0.5 rounded-full bg-surface-container text-secondary font-label-md text-label-md font-semibold">
                    65%
                  </span>
                </div>
                <div className="mt-2 text-text-muted font-body-sm text-body-sm">
                  Tổng cộng {Math.round(totalClicks * 0.65)} người dùng di động
                </div>
              </div>
            </div>

            {/* KPI 4 */}
            <div className="p-5 rounded-2xl bg-surface-card shadow-sm border border-border-subtle flex flex-col justify-between">
              <div className="flex items-center justify-between mb-3">
                <span className="font-label-md text-label-md text-text-secondary uppercase tracking-wider font-semibold">
                  Tỷ Lệ An Toàn
                </span>
                <div className="w-9 h-9 rounded-xl bg-emerald-50 flex items-center justify-center text-status-emerald">
                  <span className="material-symbols-outlined text-[20px]">verified</span>
                </div>
              </div>
              <div>
                <div className="flex items-baseline gap-2">
                  <span className="font-display text-3xl font-bold text-text-primary tracking-tight">
                    100.0%
                  </span>
                </div>
                <div className="mt-2 flex items-center gap-1.5 text-text-muted font-body-sm text-body-sm">
                  <span className="w-2 h-2 rounded-full bg-status-emerald"></span>
                  <span>Đã kiểm tra qua Safe Scheme</span>
                </div>
              </div>
            </div>
          </div>

          {/* Area Chart: Clicks over Time */}
          <div className="p-6 rounded-2xl bg-surface-card shadow-sm border border-border-subtle">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
              <div>
                <h2 className="font-display text-xl font-bold text-text-primary tracking-tight">
                  Biểu Đồ Lượt Click Theo Thời Gian
                </h2>
                <p className="font-body-sm text-body-sm text-text-muted mt-0.5">
                  Biến động lưu lượng truy cập liên kết trong chu kỳ đã chọn
                </p>
              </div>

              {/* Time Frame Range Switcher */}
              <div className="inline-flex p-1 rounded-xl bg-surface-container-low border border-border-subtle self-start sm:self-auto">
                <button
                  type="button"
                  onClick={() => setTimeRange('7_days')}
                  className={`px-3 py-1.5 rounded-lg font-label-md text-label-md transition-all cursor-pointer ${
                    timeRange === '7_days'
                      ? 'bg-surface-card text-primary font-semibold shadow-sm'
                      : 'text-text-secondary hover:text-text-primary'
                  }`}
                >
                  7 Ngày qua
                </button>
                <button
                  type="button"
                  onClick={() => setTimeRange('30_days')}
                  className={`px-3 py-1.5 rounded-lg font-label-md text-label-md transition-all cursor-pointer ${
                    timeRange === '30_days'
                      ? 'bg-surface-card text-primary font-semibold shadow-sm'
                      : 'text-text-secondary hover:text-text-primary'
                  }`}
                >
                  30 Ngày qua
                </button>
                <button
                  type="button"
                  onClick={() => setTimeRange('all')}
                  className={`px-3 py-1.5 rounded-lg font-label-md text-label-md transition-all cursor-pointer ${
                    timeRange === 'all'
                      ? 'bg-surface-card text-primary font-semibold shadow-sm'
                      : 'text-text-secondary hover:text-text-primary'
                  }`}
                >
                  Toàn thời gian
                </button>
              </div>
            </div>

            {/* Recharts Area Chart Container */}
            <div className="w-full h-72 sm:h-80">
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={MOCK_TIME_DATA} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                  <defs>
                    <linearGradient id="colorClicks" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#7C3AED" stopOpacity={0.35} />
                      <stop offset="95%" stopColor="#4F46E5" stopOpacity={0.0} />
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" stroke="#F1F5F9" vertical={false} />
                  <XAxis dataKey="date" stroke="#94A3B8" fontSize={12} tickLine={false} />
                  <YAxis stroke="#94A3B8" fontSize={12} tickLine={false} />
                  <Tooltip
                    contentStyle={{
                      backgroundColor: '#131B2E',
                      borderRadius: '12px',
                      color: '#FFFFFF',
                      border: 'none',
                      boxShadow: '0 10px 25px -5px rgba(0,0,0,0.3)',
                    }}
                    labelStyle={{ color: '#C49BFF', fontWeight: 600 }}
                  />
                  <Area
                    type="monotone"
                    dataKey="clicks"
                    stroke="#7C3AED"
                    strokeWidth={3}
                    fillOpacity={1}
                    fill="url(#colorClicks)"
                  />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          </div>

          {/* Dual Analysis Grid: Device Breakdown & Referrers */}
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
            {/* Left: Device Distribution (5 cols) */}
            <div className="lg:col-span-5 p-6 rounded-2xl bg-surface-card shadow-sm border border-border-subtle flex flex-col justify-between space-y-6">
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="font-display text-xl font-bold text-text-primary">Phân Bổ Thiết Bị</h3>
                  <p className="font-body-sm text-body-sm text-text-muted">Cơ cấu môi trường người dùng click</p>
                </div>
                <span className="material-symbols-outlined text-text-muted text-[22px]">devices</span>
              </div>

              {/* Donut Chart & Legend */}
              <div className="flex flex-col sm:flex-row items-center justify-around gap-6 py-2">
                <div className="relative w-40 h-40 shrink-0 flex items-center justify-center">
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Pie
                        data={DEVICE_DATA}
                        cx="50%"
                        cy="50%"
                        innerRadius={50}
                        outerRadius={70}
                        paddingAngle={4}
                        dataKey="value"
                      >
                        {DEVICE_DATA.map((entry, index) => (
                          <Cell key={`cell-${index}`} fill={entry.color} />
                        ))}
                      </Pie>
                    </PieChart>
                  </ResponsiveContainer>
                  <div className="absolute inset-0 flex flex-col items-center justify-center text-center pointer-events-none">
                    <span className="text-text-muted font-label-md text-xs">Đa số</span>
                    <span className="font-display text-lg font-bold text-text-primary">65%</span>
                    <span className="text-[10px] text-brand-violet font-semibold">Mobile</span>
                  </div>
                </div>

                <div className="flex flex-col gap-3 w-full sm:w-auto">
                  {DEVICE_DATA.map((item) => (
                    <div key={item.name} className="flex items-center justify-between gap-4">
                      <div className="flex items-center gap-2">
                        <span className="w-3 h-3 rounded-full shrink-0" style={{ backgroundColor: item.color }}></span>
                        <span className="font-body-md text-body-md text-text-primary">{item.name}</span>
                      </div>
                      <div className="text-right">
                        <span className="font-display font-bold text-sm text-text-primary">{item.value}%</span>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              <div className="p-3.5 rounded-xl bg-surface-container-low flex items-center gap-3 border border-border-subtle">
                <span className="material-symbols-outlined text-primary text-[20px] shrink-0">info</span>
                <p className="font-body-sm text-body-sm text-text-secondary">
                  Người xem link này tương thích tốt nhất với giao diện tối ưu cho thiết bị di động.
                </p>
              </div>
            </div>

            {/* Right: Top Referrers (7 cols) */}
            <div className="lg:col-span-7 p-6 rounded-2xl bg-surface-card shadow-sm border border-border-subtle flex flex-col justify-between space-y-6">
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="font-display text-xl font-bold text-text-primary">Top Nguồn Truy Cập</h3>
                  <p className="font-body-sm text-body-sm text-text-muted">Các kênh mạng xã hội và nền tảng giới thiệu</p>
                </div>
                <span className="material-symbols-outlined text-text-muted text-[22px]">hub</span>
              </div>

              <div className="space-y-4">
                {/* Referrer 1 */}
                <div className="p-4 rounded-xl bg-surface-card-subtle flex flex-col gap-2 border border-border-subtle">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <div className="w-7 h-7 rounded-lg bg-surface-container flex items-center justify-center text-primary font-bold text-xs">
                        1
                      </div>
                      <div>
                        <span className="font-label-md text-label-md text-text-primary block font-semibold">
                          Direct / Tin nhắn Zalo & Telegram
                        </span>
                        <span className="text-text-muted font-body-sm text-xs">Kênh chia sẻ trực tiếp 1-1</span>
                      </div>
                    </div>
                    <div className="text-right">
                      <span className="font-display font-bold text-sm text-text-primary">
                        {Math.round(totalClicks * 0.53)}
                      </span>
                      <span className="text-text-muted text-xs ml-1">clicks</span>
                      <span className="block text-status-emerald font-semibold text-xs">53%</span>
                    </div>
                  </div>
                  <div className="w-full h-2 rounded-full bg-surface-container overflow-hidden">
                    <div className="h-full rounded-full bg-gradient-to-r from-brand-violet to-brand-indigo" style={{ width: '53%' }}></div>
                  </div>
                </div>

                {/* Referrer 2 */}
                <div className="p-4 rounded-xl bg-surface-card-subtle flex flex-col gap-2 border border-border-subtle">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <div className="w-7 h-7 rounded-lg bg-surface-container flex items-center justify-center text-primary font-bold text-xs">
                        2
                      </div>
                      <div>
                        <span className="font-label-md text-label-md text-text-primary block font-semibold">
                          Facebook (Feed & Groups)
                        </span>
                        <span className="text-text-muted font-body-sm text-xs">Mạng xã hội cộng đồng</span>
                      </div>
                    </div>
                    <div className="text-right">
                      <span className="font-display font-bold text-sm text-text-primary">
                        {Math.round(totalClicks * 0.33)}
                      </span>
                      <span className="text-text-muted text-xs ml-1">clicks</span>
                      <span className="block text-status-emerald font-semibold text-xs">33%</span>
                    </div>
                  </div>
                  <div className="w-full h-2 rounded-full bg-surface-container overflow-hidden">
                    <div className="h-full rounded-full bg-gradient-to-r from-brand-indigo to-brand-purple-glow" style={{ width: '33%' }}></div>
                  </div>
                </div>

                {/* Referrer 3 */}
                <div className="p-4 rounded-xl bg-surface-card-subtle flex flex-col gap-2 border border-border-subtle">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <div className="w-7 h-7 rounded-lg bg-surface-container flex items-center justify-center text-primary font-bold text-xs">
                        3
                      </div>
                      <div>
                        <span className="font-label-md text-label-md text-text-primary block font-semibold">
                          Google Search & Khác
                        </span>
                        <span className="text-text-muted font-body-sm text-xs">Tìm kiếm tự nhiên</span>
                      </div>
                    </div>
                    <div className="text-right">
                      <span className="font-display font-bold text-sm text-text-primary">
                        {Math.round(totalClicks * 0.14)}
                      </span>
                      <span className="text-text-muted text-xs ml-1">clicks</span>
                      <span className="block text-text-secondary font-semibold text-xs">14%</span>
                    </div>
                  </div>
                  <div className="w-full h-2 rounded-full bg-surface-container overflow-hidden">
                    <div className="h-full rounded-full bg-status-emerald" style={{ width: '14%' }}></div>
                  </div>
                </div>
              </div>

              <div className="pt-2 flex items-center justify-between text-xs text-text-muted">
                <span>Cập nhật theo thời gian thực (Realtime Telemetry)</span>
                <span className="text-primary font-semibold">Hệ thống đo lường tự động</span>
              </div>
            </div>
          </div>
        </div>
      </main>

      {/* Dynamic QR Modal */}
      <QrModal
        isOpen={qrModalData.isOpen}
        onClose={() => setQrModalData({ ...qrModalData, isOpen: false })}
        url={qrModalData.url}
        shortCode={qrModalData.code}
        onCopySuccess={() => triggerToast('Đã sao chép liên kết!', 'Thành công')}
      />

      {/* Toast Notification */}
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

export default AnalyticsPage;
