import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Area, AreaChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import QrModal from '../components/QrModal';
import Toast from '../components/Toast';
import { MetricCard, StatsList } from '../components/AnalyticsStats';
import { useToast } from '../hooks/useToast';
import { urlApi } from '../api/urlApi';
import { csvCell, getErrorMessage, getUrlStatus } from '../utils/urlUtils';
import { filterTimeSeries } from '../utils/analyticsUtils';

/** Trang điều phối API và state; biểu đồ và bảng chỉ nhận dữ liệu thật từ backend. */
export default function AnalyticsPage() {
  const { id } = useParams();
  const [url, setUrl] = useState(null);
  const [analytics, setAnalytics] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [range, setRange] = useState('7');
  const [refreshCount, setRefreshCount] = useState(0);
  const [qrOpen, setQrOpen] = useState(false);
  const { toast, triggerToast } = useToast();

  useEffect(() => {
    const controller = new AbortController();
    async function loadAnalytics() {
      setIsLoading(true);
      setError('');
      try {
        // Hai endpoint độc lập: tải song song, dùng cùng signal để huỷ khi rời trang.
        const config = { signal: controller.signal };
        const [urlData, statistics] = await Promise.all([
          urlApi.getUrlById(id, config), urlApi.getAnalytics(id, config),
        ]);
        if (controller.signal.aborted) return;
        setUrl(urlData);
        setAnalytics(statistics);
      } catch (requestError) {
        if (!controller.signal.aborted) {
          setError(getErrorMessage(requestError, 'Không thể tải thống kê. Vui lòng thử lại.'));
        }
      } finally {
        if (!controller.signal.aborted) setIsLoading(false);
      }
    }
    loadAnalytics();
    return () => controller.abort();
  }, [id, refreshCount]);

  async function handleCopyLink() {
    try {
      await navigator.clipboard.writeText(url.shortUrl);
      triggerToast('Đã sao chép liên kết');
    } catch {
      triggerToast('Trình duyệt không cho phép sao chép. Bạn có thể chọn và sao chép đường dẫn.', 'Lỗi', 'error');
    }
  }

  const chartData = filterTimeSeries(analytics?.clicksOverTime || [], range);

  function handleExportCsv() {
    const rows = [['Date (UTC)', 'Clicks', 'Original URL', 'Short URL'],
      ...chartData.map((point) => [point.timestamp, point.clicks, url.originalUrl, url.shortUrl])];
    const csv = rows.map((row) => row.map(csvCell).join(',')).join('\r\n');
    // Blob giữ đúng Unicode, không giới hạn bởi độ dài data URL.
    const objectUrl = URL.createObjectURL(new Blob(['\uFEFF', csv], { type: 'text/csv;charset=utf-8' }));
    const link = document.createElement('a');
    link.href = objectUrl;
    link.download = `snaplink-analytics-${url.shortCode}.csv`;
    link.click();
    setTimeout(() => URL.revokeObjectURL(objectUrl), 1000);
    triggerToast('Đã xuất báo cáo CSV');
  }

  return (
    <div className="min-h-screen flex flex-col bg-canvas-bg bg-dot-matrix">
      <Navbar />
      <main className="flex-1 w-full max-w-7xl mx-auto px-4 pt-24 pb-12 space-y-6">
        <div className="flex flex-wrap justify-between gap-3">
          <Link to="/dashboard" className="text-brand-violet">← Quay lại Dashboard</Link>
          <button type="button" disabled={isLoading} onClick={() => setRefreshCount((count) => count + 1)}
            className="text-brand-violet disabled:opacity-50">Làm mới dữ liệu</button>
        </div>
        {isLoading ? <p role="status">Đang tải thống kê...</p> : error ? (
          <p role="alert" className="p-5 rounded-xl bg-surface-card text-status-rose">{error}</p>
        ) : url && analytics && (
          <>
            <section className="p-6 rounded-2xl bg-surface-card border border-border-subtle space-y-3">
              <h1 className="text-2xl font-bold break-all">{url.shortUrl}</h1>
              <p className="text-text-secondary break-all">Link gốc: <a href={url.originalUrl} target="_blank" rel="noreferrer">{url.originalUrl}</a></p>
              <p>{getUrlStatus(url) === 'EXPIRED' ? 'Đã hết hạn' : url.isActive ? 'Đang hoạt động' : 'Đã vô hiệu hoá'}</p>
              <div className="flex flex-wrap gap-4 text-brand-violet">
                <button type="button" onClick={handleCopyLink}>Sao chép</button>
                <button type="button" onClick={() => setQrOpen(true)}>Mã QR</button>
                <button type="button" onClick={handleExportCsv}>Xuất CSV</button>
              </div>
            </section>
            <div className="grid sm:grid-cols-2 gap-4">
              <MetricCard title="Tổng lượt click" value={analytics.totalClicks.toLocaleString()} description="Toàn thời gian; click được ghi nhận bất đồng bộ." />
              <MetricCard title="IP truy cập khác nhau" value={analytics.uniqueVisitors.toLocaleString()} description="Ước lượng theo IP, không đồng nghĩa số người dùng thực tế." />
            </div>
            <section className="p-6 rounded-2xl bg-surface-card border border-border-subtle">
              <div className="flex flex-wrap justify-between gap-4 mb-6">
                <h2 className="text-xl font-bold">Lượt click theo ngày (UTC)</h2>
                <select aria-label="Khoảng thời gian biểu đồ" value={range} onChange={(event) => setRange(event.target.value)}
                  className="bg-surface-container rounded p-2">
                  <option value="7">7 ngày qua</option><option value="30">30 ngày qua</option><option value="all">Toàn thời gian</option>
                </select>
              </div>
              {!chartData.length ? <p>Chưa có lượt click trong khoảng đã chọn.</p> : (
                <div className="h-72 w-full">
                  <ResponsiveContainer width="100%" height="100%">
                    <AreaChart data={chartData}>
                      <CartesianGrid strokeDasharray="3 3" /><XAxis dataKey="timestamp" /><YAxis allowDecimals={false} />
                      <Tooltip /><Area dataKey="clicks" stroke="#7C3AED" fill="#DDD6FE" />
                    </AreaChart>
                  </ResponsiveContainer>
                </div>
              )}
            </section>
            <p className="text-sm text-text-muted">Các phân bổ bên dưới là số liệu toàn thời gian.</p>
            <div className="grid md:grid-cols-2 gap-6">
              <StatsList title="Thiết bị" items={analytics.devices} />
              <StatsList title="Trình duyệt" items={analytics.browsers} />
              <StatsList title="Quốc gia" items={analytics.countries} />
              <StatsList title="Nguồn truy cập" items={analytics.referrers} />
            </div>
          </>
        )}
      </main>
      {url && <QrModal isOpen={qrOpen} onClose={() => setQrOpen(false)} url={url.shortUrl} shortCode={url.shortCode} onCopySuccess={() => triggerToast('Đã sao chép liên kết')} />}
      <Toast {...toast} />
      <Footer />
    </div>
  );
}
