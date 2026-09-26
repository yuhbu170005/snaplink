import React, { useState, useEffect, useMemo } from 'react';
import { Link } from 'react-router-dom';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import QrModal from '../components/QrModal';
import DeleteModal from '../components/DeleteModal';
import Toast from '../components/Toast';
import { urlApi } from '../api/urlApi';

const DashboardPage = () => {
  const [urls, setUrls] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [isLoading, setIsLoading] = useState(true);

  // Filters & Search
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('Tất cả'); // 'Tất cả' | 'Đang hoạt động' | 'Hết hạn' / 'Vô hiệu hóa'
  const [isFilterDropdownOpen, setIsFilterDropdownOpen] = useState(false);

  // Quick Create Modal
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [createForm, setCreateForm] = useState({ originalUrl: '', customAlias: '', expiresAt: '' });
  const [isCreating, setIsCreating] = useState(false);

  // Delete Modal
  const [deleteModalData, setDeleteModalData] = useState({ isOpen: false, id: null, code: '', isLoading: false });

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

  const fetchUrls = async (currentPage = 0) => {
    try {
      setIsLoading(true);
      const data = await urlApi.getUrls({ page: currentPage, size: 10 });
      if (data && data.content) {
        setUrls(data.content);
        setPage(data.page || 0);
        setTotalPages(data.totalPages || 1);
        setTotalElements(data.totalElements || data.content.length);
      } else if (Array.isArray(data)) {
        setUrls(data);
        setTotalElements(data.length);
      }
    } catch (err) {
      console.error('Error fetching URLs:', err);
      triggerToast('Không thể tải danh sách liên kết', 'Lỗi', 'error');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchUrls(page);
  }, [page]);

  // Status Switch Toggle
  const handleToggleStatus = async (item) => {
    const newStatus = item.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    try {
      await urlApi.updateStatus(item.id, newStatus);
      setUrls((prev) =>
        prev.map((u) => (u.id === item.id ? { ...u, status: newStatus } : u))
      );
      triggerToast(
        `Đã chuyển trạng thái liên kết sang: ${newStatus === 'ACTIVE' ? 'Kích hoạt (Active)' : 'Vô hiệu hoá (Inactive)'}`,
        'Cập nhật trạng thái'
      );
    } catch (err) {
      triggerToast('Không thể cập nhật trạng thái liên kết', 'Lỗi', 'error');
    }
  };

  // Create Link
  const handleCreateSubmit = async (e) => {
    e.preventDefault();
    if (!createForm.originalUrl) return;

    try {
      setIsCreating(true);
      const payload = {
        originalUrl: createForm.originalUrl.trim(),
        customAlias: createForm.customAlias.trim() || undefined,
        expiresAt: createForm.expiresAt ? new Date(createForm.expiresAt).toISOString() : undefined,
      };

      await urlApi.createUrl(payload);
      triggerToast('Tạo link rút gọn mới thành công!', 'Thành công');
      setIsCreateModalOpen(false);
      setCreateForm({ originalUrl: '', customAlias: '', expiresAt: '' });
      fetchUrls(0);
    } catch (err) {
      const msg = err.response?.data?.message || 'Không thể tạo link mới.';
      triggerToast(msg, 'Lỗi', 'error');
    } finally {
      setIsCreating(false);
    }
  };

  // Delete Link
  const handleDeleteConfirm = async () => {
    if (!deleteModalData.id) return;
    try {
      setDeleteModalData((prev) => ({ ...prev, isLoading: true }));
      await urlApi.deleteUrl(deleteModalData.id);
      setUrls((prev) => prev.filter((u) => u.id !== deleteModalData.id));
      setTotalElements((prev) => Math.max(0, prev - 1));
      triggerToast('Đã xoá liên kết thành công!', 'Đã xoá', 'success');
      setDeleteModalData({ isOpen: false, id: null, code: '', isLoading: false });
    } catch (err) {
      triggerToast('Không thể xoá liên kết', 'Lỗi', 'error');
      setDeleteModalData((prev) => ({ ...prev, isLoading: false }));
    }
  };

  // Filtered & Searched URLs
  const filteredUrls = useMemo(() => {
    return urls.filter((item) => {
      const code = (item.customAlias || item.shortCode || '').toLowerCase();
      const orig = (item.originalUrl || '').toLowerCase();
      const term = searchTerm.toLowerCase();
      const matchesSearch = code.includes(term) || orig.includes(term);

      if (!matchesSearch) return false;

      if (statusFilter === 'Đang hoạt động') {
        return item.status === 'ACTIVE';
      }
      if (statusFilter === 'Hết hạn') {
        return item.status === 'INACTIVE' || (item.expiresAt && new Date(item.expiresAt) < new Date());
      }
      return true;
    });
  }, [urls, searchTerm, statusFilter]);

  // Metrics Calculation
  const metrics = useMemo(() => {
    const total = totalElements || urls.length;
    const totalClicks = urls.reduce((sum, u) => sum + (u.totalClicks || 0), 0);
    const activeCount = urls.filter((u) => u.status === 'ACTIVE').length;
    const activeRate = total > 0 ? ((activeCount / total) * 100).toFixed(1) : '100.0';
    return { total, totalClicks, activeCount, activeRate };
  }, [urls, totalElements]);

  return (
    <div className="min-h-screen flex flex-col bg-canvas-bg bg-dot-matrix relative">
      <Navbar onQuickShorten={() => setIsCreateModalOpen(true)} />

      <main className="flex-1 w-full pt-20 pb-12">
        <div className="w-full max-w-7xl mx-auto px-4 sm:px-6 flex flex-col gap-6">
          {/* Top Header & Search Bar */}
          <div className="flex flex-col md:flex-row md:items-end justify-between gap-4 bg-surface-card p-6 rounded-2xl shadow-sm border border-border-subtle">
            <div className="flex flex-col gap-1">
              <div className="flex items-center gap-1 text-brand-violet font-label-md text-label-md">
                <span className="material-symbols-outlined text-[16px]">link</span>
                <span className="tracking-wider uppercase font-semibold">Trung tâm điều khiển</span>
              </div>
              <h1 className="font-display text-3xl font-bold text-text-primary tracking-tight">
                Quản lý Liên Kết
              </h1>
              <p className="font-body-md text-body-md text-text-secondary">
                Theo dõi, chỉnh sửa và quản lý tất cả các short link đã tạo của bạn
              </p>
            </div>

            <div className="flex flex-wrap items-center gap-3">
              {/* Search Bar */}
              <div className="relative flex items-center min-w-[240px] bg-surface-card-subtle rounded-xl px-3 py-2 text-text-secondary border border-border-subtle focus-within:border-brand-violet focus-within:bg-surface-card shadow-sm transition-all">
                <span className="material-symbols-outlined text-[18px] mr-2 text-text-muted">search</span>
                <input
                  type="text"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  placeholder="Tìm kiếm link, alias, URL..."
                  className="bg-transparent border-0 outline-none w-full font-body-sm text-body-sm text-text-primary placeholder:text-text-muted"
                />
              </div>

              {/* Filter Dropdown */}
              <div className="relative">
                <button
                  type="button"
                  onClick={() => setIsFilterDropdownOpen(!isFilterDropdownOpen)}
                  className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-surface-card-subtle hover:bg-surface-container text-text-primary font-label-md text-label-md transition-colors border border-border-subtle cursor-pointer"
                >
                  <span className="material-symbols-outlined text-[18px] text-text-secondary">filter_list</span>
                  <span>{statusFilter}</span>
                  <span className="material-symbols-outlined text-[16px] text-text-muted">expand_more</span>
                </button>

                {isFilterDropdownOpen && (
                  <div className="absolute right-0 mt-1 w-44 bg-surface-card rounded-xl shadow-xl z-30 py-1 border border-border-subtle">
                    {['Tất cả', 'Đang hoạt động', 'Hết hạn'].map((opt) => (
                      <button
                        key={opt}
                        type="button"
                        onClick={() => {
                          setStatusFilter(opt);
                          setIsFilterDropdownOpen(false);
                        }}
                        className="w-full text-left px-3 py-2 text-label-md text-text-primary hover:bg-surface-card-subtle flex items-center justify-between cursor-pointer"
                      >
                        <span>{opt}</span>
                        {statusFilter === opt && (
                          <span className="material-symbols-outlined text-[16px] text-status-emerald">check</span>
                        )}
                      </button>
                    ))}
                  </div>
                )}
              </div>

              {/* Create Link Primary CTA */}
              <button
                type="button"
                onClick={() => setIsCreateModalOpen(true)}
                className="inline-flex items-center gap-1.5 px-4 py-2.5 rounded-xl text-on-primary font-label-lg text-label-lg shadow-md hover:brightness-105 active:scale-98 transition-all shrink-0 cursor-pointer bg-brand-violet"
              >
                <span className="material-symbols-outlined text-[18px]">add_circle</span>
                <span>+ Tạo Link Mới</span>
              </button>
            </div>
          </div>

          {/* 3 Metric KPI Cards */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {/* KPI 1 */}
            <div className="bg-surface-card rounded-2xl p-6 shadow-sm border border-border-subtle flex flex-col justify-between transition-transform hover:-translate-y-0.5">
              <div className="flex items-center justify-between">
                <span className="font-label-md text-label-md uppercase tracking-wider text-text-secondary font-semibold">
                  Tổng Số Link
                </span>
                <div className="w-10 h-10 rounded-xl bg-surface-container flex items-center justify-center text-brand-violet">
                  <span className="material-symbols-outlined text-[22px]">link</span>
                </div>
              </div>
              <div className="mt-4 flex items-baseline justify-between">
                <span className="font-display text-3xl font-bold text-text-primary">
                  {metrics.total}
                </span>
                <span className="inline-flex items-center gap-1 font-label-md text-label-md text-brand-violet bg-surface-container px-2 py-0.5 rounded-full">
                  <span className="material-symbols-outlined text-[14px]">trending_up</span>
                  Active
                </span>
              </div>
              <div className="w-full bg-surface-card-subtle h-1.5 rounded-full mt-3 overflow-hidden">
                <div className="bg-brand-violet h-full rounded-full" style={{ width: '100%' }}></div>
              </div>
            </div>

            {/* KPI 2 */}
            <div className="bg-surface-card rounded-2xl p-6 shadow-sm border border-border-subtle flex flex-col justify-between transition-transform hover:-translate-y-0.5">
              <div className="flex items-center justify-between">
                <span className="font-label-md text-label-md uppercase tracking-wider text-text-secondary font-semibold">
                  Tổng Lượt Click
                </span>
                <div className="w-10 h-10 rounded-xl bg-surface-container-low flex items-center justify-center text-status-emerald">
                  <span className="material-symbols-outlined text-[22px]">bar_chart</span>
                </div>
              </div>
              <div className="mt-4 flex items-baseline justify-between">
                <span className="font-display text-3xl font-bold text-text-primary">
                  {metrics.totalClicks.toLocaleString()}
                </span>
                <span className="inline-flex items-center gap-1 font-label-md text-label-md text-status-emerald bg-emerald-50 px-2 py-0.5 rounded-full border border-emerald-200">
                  <span className="material-symbols-outlined text-[14px]">arrow_upward</span>
                  Telemetry
                </span>
              </div>
              <div className="w-full bg-surface-card-subtle h-1.5 rounded-full mt-3 overflow-hidden">
                <div className="bg-status-emerald h-full rounded-full" style={{ width: '80%' }}></div>
              </div>
            </div>

            {/* KPI 3 */}
            <div className="bg-surface-card rounded-2xl p-6 shadow-sm border border-border-subtle flex flex-col justify-between transition-transform hover:-translate-y-0.5">
              <div className="flex items-center justify-between">
                <span className="font-label-md text-label-md uppercase tracking-wider text-text-secondary font-semibold">
                  Link Đang Hoạt Động
                </span>
                <div className="w-10 h-10 rounded-xl bg-surface-container flex items-center justify-center text-primary">
                  <span className="material-symbols-outlined text-[22px]">bolt</span>
                </div>
              </div>
              <div className="mt-4 flex items-baseline justify-between">
                <div className="flex items-baseline gap-1">
                  <span className="font-display text-3xl font-bold text-text-primary">
                    {metrics.activeCount}
                  </span>
                  <span className="font-body-md text-body-md text-text-muted">
                    / {metrics.total} link
                  </span>
                </div>
                <span className="inline-flex items-center gap-1.5 font-label-md text-label-md text-status-emerald bg-emerald-50 px-2.5 py-0.5 rounded-full border border-emerald-200">
                  <span className="w-2 h-2 rounded-full bg-status-emerald animate-ping"></span>
                  {metrics.activeRate}% Active
                </span>
              </div>
              <div className="w-full bg-surface-card-subtle h-1.5 rounded-full mt-3 overflow-hidden">
                <div
                  className="bg-brand-indigo h-full rounded-full transition-all duration-500"
                  style={{ width: `${metrics.activeRate}%` }}
                ></div>
              </div>
            </div>
          </div>

          {/* Data Table Container */}
          <div className="bg-surface-card rounded-2xl shadow-sm border border-border-subtle overflow-hidden flex flex-col">
            {/* Table Header Row */}
            <div className="p-4 px-6 bg-surface-card flex items-center justify-between border-b border-border-subtle">
              <div className="flex items-center gap-2">
                <span className="material-symbols-outlined text-brand-violet text-[20px]">dataset</span>
                <h2 className="font-display text-xl font-bold text-text-primary tracking-tight">
                  Danh Sách Liên Kết
                </h2>
              </div>
              <div className="inline-flex items-center gap-2">
                <span className="font-label-md text-label-md text-text-muted">Tự động đồng bộ REST API</span>
                <span className="inline-block w-2 h-2 rounded-full bg-status-emerald"></span>
              </div>
            </div>

            {/* Table */}
            <div className="w-full overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="bg-surface-card-subtle text-text-secondary font-label-md text-label-md uppercase tracking-wider border-b border-border-subtle">
                    <th scope="col" className="py-3 px-6">Mã Ngắn</th>
                    <th scope="col" className="py-3 px-4">Link Gốc</th>
                    <th scope="col" className="py-3 px-4">Ngày Tạo</th>
                    <th scope="col" className="py-3 px-4">Hạn Dùng</th>
                    <th scope="col" className="py-3 px-4 text-center">Trạng Thái</th>
                    <th scope="col" className="py-3 px-6 text-right">Hành Động</th>
                  </tr>
                </thead>
                <tbody className="font-body-sm text-body-sm text-text-primary divide-y divide-border-subtle">
                  {isLoading ? (
                    <tr>
                      <td colSpan={6} className="py-12 text-center text-text-muted">
                        <div className="flex flex-col items-center justify-center gap-2">
                          <div className="w-8 h-8 border-3 border-brand-violet border-t-transparent rounded-full animate-spin"></div>
                          <span>Đang tải dữ liệu liên kết...</span>
                        </div>
                      </td>
                    </tr>
                  ) : filteredUrls.length === 0 ? (
                    <tr>
                      <td colSpan={6} className="py-12 text-center text-text-muted">
                        <div className="flex flex-col items-center justify-center gap-2">
                          <span className="material-symbols-outlined text-4xl text-text-muted">link_off</span>
                          <span className="font-medium text-text-secondary">Chưa có liên kết nào phù hợp</span>
                          <button
                            type="button"
                            onClick={() => setIsCreateModalOpen(true)}
                            className="mt-2 text-sm text-brand-violet hover:underline font-semibold"
                          >
                            + Tạo liên kết đầu tiên của bạn
                          </button>
                        </div>
                      </td>
                    </tr>
                  ) : (
                    filteredUrls.map((item) => {
                      const displayCode = item.customAlias || item.shortCode;
                      const shortUrl = item.shortUrl || `https://snaplink.io/${displayCode}`;
                      const isActive = item.status === 'ACTIVE';

                      return (
                        <tr
                          key={item.id}
                          className={`hover:bg-surface-card-subtle transition-colors group ${
                            !isActive ? 'opacity-70 bg-slate-50/50' : ''
                          }`}
                        >
                          {/* Short Code */}
                          <td className="py-4 px-6 font-mono font-medium">
                            <a
                              href={shortUrl}
                              target="_blank"
                              rel="noopener noreferrer"
                              className="inline-flex items-center gap-1 text-brand-violet hover:text-brand-indigo font-semibold"
                            >
                              <span>snaplink.io/{displayCode}</span>
                              <span className="material-symbols-outlined text-[15px] opacity-70 group-hover:translate-x-0.5 transition-transform">
                                open_in_new
                              </span>
                            </a>
                          </td>

                          {/* Original URL */}
                          <td className="py-4 px-4 max-w-xs md:max-w-sm">
                            <div className="relative group/tooltip">
                              <span className="truncate block text-text-secondary font-mono text-xs max-w-[280px]">
                                {item.originalUrl}
                              </span>
                              <div className="hidden group-hover/tooltip:block absolute left-0 bottom-full mb-1 z-30 px-3 py-1.5 bg-inverse-surface text-inverse-on-surface font-mono text-xs rounded-lg shadow-lg whitespace-nowrap max-w-md truncate">
                                {item.originalUrl}
                              </div>
                            </div>
                          </td>

                          {/* Created Date */}
                          <td className="py-4 px-4 text-text-secondary whitespace-nowrap font-mono text-xs">
                            {item.createdAt
                              ? new Date(item.createdAt).toLocaleDateString('vi-VN')
                              : 'Vừa xong'}
                          </td>

                          {/* Expiration */}
                          <td className="py-4 px-4 whitespace-nowrap">
                            {item.expiresAt ? (
                              <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full font-label-md text-xs bg-amber-50 text-status-amber border border-amber-200">
                                <span className="material-symbols-outlined text-[14px]">timer</span>
                                {new Date(item.expiresAt).toLocaleDateString('vi-VN')}
                              </span>
                            ) : (
                              <span className="inline-flex items-center gap-1 text-text-muted font-label-md text-xs">
                                <span className="material-symbols-outlined text-[16px]">all_inclusive</span>
                                Không hạn
                              </span>
                            )}
                          </td>

                          {/* Status Switch Toggle */}
                          <td className="py-4 px-4 text-center">
                            <button
                              type="button"
                              role="switch"
                              aria-checked={isActive}
                              onClick={() => handleToggleStatus(item)}
                              className={`relative inline-flex h-6 w-11 shrink-0 cursor-pointer rounded-full transition-colors duration-200 ease-in-out focus:outline-none ${
                                isActive ? 'bg-status-emerald' : 'bg-outline-variant'
                              }`}
                            >
                              <span className="sr-only">Kích hoạt liên kết</span>
                              <span
                                className={`pointer-events-none inline-block h-5 w-5 transform rounded-full bg-surface-card shadow-sm transition duration-200 ease-in-out my-0.5 ml-0.5 ${
                                  isActive ? 'translate-x-5' : 'translate-x-0'
                                }`}
                              />
                            </button>
                          </td>

                          {/* Action Buttons */}
                          <td className="py-4 px-6 text-right whitespace-nowrap">
                            <div className="flex items-center justify-end gap-1">
                              {/* Copy */}
                              <button
                                type="button"
                                title="Sao chép liên kết"
                                onClick={() => {
                                  navigator.clipboard.writeText(shortUrl);
                                  triggerToast(`Đã sao chép: ${shortUrl}`, 'Thành công');
                                }}
                                className="p-2 rounded-lg text-text-secondary hover:text-brand-violet hover:bg-surface-container transition-colors cursor-pointer"
                              >
                                <span className="material-symbols-outlined text-[18px]">content_copy</span>
                              </button>

                              {/* QR Preview */}
                              <button
                                type="button"
                                title="Xem mã QR"
                                onClick={() =>
                                  setQrModalData({ isOpen: true, url: shortUrl, code: displayCode })
                                }
                                className="p-2 rounded-lg text-text-secondary hover:text-primary hover:bg-surface-container transition-colors cursor-pointer"
                              >
                                <span className="material-symbols-outlined text-[18px]">qr_code_2</span>
                              </button>

                              {/* Analytics */}
                              <Link
                                to={`/analytics/${item.id}`}
                                title="Xem thống kê"
                                className="p-2 rounded-lg text-text-secondary hover:text-status-emerald hover:bg-surface-container-low transition-colors inline-flex cursor-pointer"
                              >
                                <span className="material-symbols-outlined text-[18px]">query_stats</span>
                              </Link>

                              {/* Delete */}
                              <button
                                type="button"
                                title="Xoá liên kết"
                                onClick={() =>
                                  setDeleteModalData({
                                    isOpen: true,
                                    id: item.id,
                                    code: `snaplink.io/${displayCode}`,
                                    isLoading: false,
                                  })
                                }
                                className="p-2 rounded-lg text-text-secondary hover:text-error hover:bg-error-container/30 transition-colors cursor-pointer"
                              >
                                <span className="material-symbols-outlined text-[18px]">delete</span>
                              </button>
                            </div>
                          </td>
                        </tr>
                      );
                    })
                  )}
                </tbody>
              </table>
            </div>

            {/* Pagination Bar */}
            <div className="p-4 px-6 bg-surface-card flex flex-col sm:flex-row items-center justify-between gap-4 border-t border-border-subtle">
              <span className="font-body-sm text-body-sm text-text-secondary">
                Hiển thị <span className="font-semibold text-text-primary">{filteredUrls.length}</span> trên tổng số{' '}
                <span className="font-semibold text-text-primary">{totalElements}</span> liên kết
              </span>
              <div className="flex items-center gap-2">
                <button
                  type="button"
                  disabled={page <= 0}
                  onClick={() => setPage((p) => Math.max(0, p - 1))}
                  className="inline-flex items-center gap-1 px-3 py-1.5 rounded-xl bg-surface-card-subtle hover:bg-surface-container text-text-primary font-label-md text-label-md transition-colors border border-border-subtle disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer"
                >
                  <span className="material-symbols-outlined text-[16px]">chevron_left</span>
                  Trang Trước
                </button>
                <div className="flex items-center px-3 py-1 bg-surface-container rounded-xl font-label-md text-label-md text-brand-violet font-semibold">
                  Trang {page + 1} / {totalPages || 1}
                </div>
                <button
                  type="button"
                  disabled={page >= totalPages - 1}
                  onClick={() => setPage((p) => p + 1)}
                  className="inline-flex items-center gap-1 px-3 py-1.5 rounded-xl bg-surface-card-subtle hover:bg-surface-container text-text-primary hover:text-brand-violet font-label-md text-label-md transition-colors border border-border-subtle disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer"
                >
                  Trang Sau
                  <span className="material-symbols-outlined text-[16px]">chevron_right</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </main>

      {/* Quick Create Link Modal */}
      {isCreateModalOpen && (
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
                onClick={() => setIsCreateModalOpen(false)}
                className="text-text-muted hover:text-text-primary p-1 rounded-lg"
              >
                <span className="material-symbols-outlined">close</span>
              </button>
            </div>

            <form className="flex flex-col gap-4" onSubmit={handleCreateSubmit}>
              <div className="flex flex-col gap-1.5">
                <label className="font-label-md text-label-md text-text-secondary">
                  Đường dẫn gốc (Original URL) *
                </label>
                <div className="flex items-center bg-surface-card-subtle px-3 py-2.5 rounded-xl border border-border-subtle focus-within:border-brand-violet focus-within:bg-surface-card shadow-sm">
                  <span className="material-symbols-outlined text-text-muted mr-2 text-[18px]">link</span>
                  <input
                    type="url"
                    required
                    value={createForm.originalUrl}
                    onChange={(e) => setCreateForm({ ...createForm, originalUrl: e.target.value })}
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
                    <span className="font-mono text-xs text-text-muted pr-1">snaplink.io/</span>
                    <input
                      type="text"
                      value={createForm.customAlias}
                      onChange={(e) => setCreateForm({ ...createForm, customAlias: e.target.value })}
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
                      type="date"
                      value={createForm.expiresAt}
                      onChange={(e) => setCreateForm({ ...createForm, expiresAt: e.target.value })}
                      className="w-full bg-transparent border-0 outline-none font-body-sm text-body-sm text-text-primary"
                    />
                  </div>
                </div>
              </div>

              <div className="flex items-center justify-end gap-3 mt-4">
                <button
                  type="button"
                  onClick={() => setIsCreateModalOpen(false)}
                  className="px-4 py-2.5 rounded-xl bg-surface-card-subtle hover:bg-surface-container text-text-secondary font-label-lg text-label-lg border border-border-subtle cursor-pointer"
                >
                  Đóng
                </button>
                <button
                  type="submit"
                  disabled={isCreating}
                  className="px-5 py-2.5 rounded-xl text-on-primary font-label-lg text-label-lg shadow-md hover:brightness-105 bg-brand-violet cursor-pointer disabled:opacity-75"
                >
                  {isCreating ? 'Đang tạo...' : 'Tạo Ngay'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Delete Confirmation Modal */}
      <DeleteModal
        isOpen={deleteModalData.isOpen}
        onClose={() => setDeleteModalData({ isOpen: false, id: null, code: '', isLoading: false })}
        onConfirm={handleDeleteConfirm}
        targetName={deleteModalData.code}
        isLoading={deleteModalData.isLoading}
      />

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

export default DashboardPage;
