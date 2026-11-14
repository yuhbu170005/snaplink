import CreateUrlModal from '../components/CreateUrlModal';
import { useToast } from '../hooks/useToast';
import { createUrlPayload, getErrorMessage, getUrlStatus, getShortUrl } from '../utils/urlUtils';
import React, { useState, useEffect, useMemo, useCallback } from 'react';
import { Link } from 'react-router-dom';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import QrModal from '../components/QrModal';
import DeleteModal from '../components/DeleteModal';
import Toast from '../components/Toast';
import { urlApi } from '../api/urlApi';

/** Điều phối danh sách phân trang, lọc trong trang, tạo/xoá/toggle link và các modal. */
const DashboardPage = () => {
  const [urls, setUrls] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [pendingStatusId, setPendingStatusId] = useState(null);

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
  const { toast, triggerToast } = useToast();

  /** Response dùng pageNumber, isActive đúng theo DTO backend. */
  const fetchUrls = useCallback(async (currentPage = 0, signal) => {
    setIsLoading(true);
    try {
      const data = await urlApi.getUrls({ page: currentPage, size: 10 }, { signal });
      if (signal?.aborted) return;
      setUrls(data.content);
      setTotalPages(data.totalPages);
      setTotalElements(data.totalElements);
    } catch (error) {
      if (signal?.aborted) return;
      triggerToast(getErrorMessage(error, 'Không thể tải danh sách liên kết'), 'Lỗi', 'error');
    } finally {
      if (!signal?.aborted) setIsLoading(false);
    }
  }, [triggerToast]);

  useEffect(() => {
    const controller = new AbortController();
    fetchUrls(page, controller.signal);
    // Đổi trang nhanh: huỷ request cũ để nó không ghi đè dữ liệu trang mới.
    return () => controller.abort();
  }, [page, fetchUrls]);

  async function handleToggleStatus(item) {
    if (pendingStatusId !== null) return;
    setPendingStatusId(item.id);
    try {
      const updatedUrl = await urlApi.toggleStatus(item.id);
      setUrls((previous) => previous.map((url) => url.id === item.id ? updatedUrl : url));
      triggerToast(updatedUrl.isActive ? 'Đã kích hoạt liên kết' : 'Đã vô hiệu hoá liên kết');
    } catch (error) {
      triggerToast(getErrorMessage(error, 'Không thể cập nhật trạng thái liên kết'), 'Lỗi', 'error');
    } finally {
      setPendingStatusId(null);
    }
  }

  // Create Link
  // Tạo link rồi tải lại trang đầu để hiển thị link mới nhất.
  const handleCreateSubmit = async (e) => {
    e.preventDefault();
    if (!createForm.originalUrl) return;

    try {
      setIsCreating(true);
      const payload = createUrlPayload(createForm);

      await urlApi.createUrl(payload);
      triggerToast('Tạo link rút gọn mới thành công!', 'Thành công');
      setIsCreateModalOpen(false);
      setCreateForm({ originalUrl: '', customAlias: '', expiresAt: '' });
      if (page === 0) await fetchUrls(0);
      else setPage(0);
    } catch (err) {
      const msg = getErrorMessage(err, 'Không thể tạo link mới.');
      triggerToast(msg, 'Lỗi', 'error');
    } finally {
      setIsCreating(false);
    }
  };

  // Delete Link
  // Chỉ xoá sau xác nhận và cập nhật lại phân trang khi trang hiện tại rỗng.
  const handleDeleteConfirm = async () => {
    if (!deleteModalData.id) return;
    try {
      setDeleteModalData((prev) => ({ ...prev, isLoading: true }));
      await urlApi.deleteUrl(deleteModalData.id);
      // Xoá phần tử cuối trang: quay lại trang trước; còn lại thì tải trang hiện tại.
      if (urls.length === 1 && page > 0) setPage(page - 1);
      else await fetchUrls(page);
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
        return getUrlStatus(item) === 'ACTIVE';
      }
      if (statusFilter === 'Hết hạn') {
        return getUrlStatus(item) === 'EXPIRED';
      }
      if (statusFilter === 'Vô hiệu hoá') return getUrlStatus(item) === 'INACTIVE';
      return true;
    });
  }, [urls, searchTerm, statusFilter]);

  // Metrics Calculation
  const metrics = useMemo(() => {
    const total = totalElements;
    const activeCount = urls.filter((url) => getUrlStatus(url) === 'ACTIVE').length;
    const activeRate = urls.length ? ((activeCount / urls.length) * 100).toFixed(1) : '0.0';
    return { total, pageCount: urls.length, activeCount, activeRate };
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
                  placeholder="Tìm trong trang hiện tại..."
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
                    {['Tất cả', 'Đang hoạt động', 'Hết hạn', 'Vô hiệu hoá'].map((opt) => (
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
                  Link Trong Trang
                </span>
                <div className="w-10 h-10 rounded-xl bg-surface-container-low flex items-center justify-center text-status-emerald">
                  <span className="material-symbols-outlined text-[22px]">bar_chart</span>
                </div>
              </div>
              <div className="mt-4 flex items-baseline justify-between">
                <span className="font-display text-3xl font-bold text-text-primary">
                  {metrics.pageCount}
                </span>
                <span className="inline-flex items-center gap-1 font-label-md text-label-md text-status-emerald bg-emerald-50 px-2 py-0.5 rounded-full border border-emerald-200">
                  <span className="material-symbols-outlined text-[14px]">arrow_upward</span>
                  Trang hiện tại
                </span>
              </div>
              <div className="w-full bg-surface-card-subtle h-1.5 rounded-full mt-3 overflow-hidden">
                <div className="bg-status-emerald h-full rounded-full" style={{ width: urls.length ? '100%' : '0%' }}></div>
              </div>
            </div>

            {/* KPI 3 */}
            <div className="bg-surface-card rounded-2xl p-6 shadow-sm border border-border-subtle flex flex-col justify-between transition-transform hover:-translate-y-0.5">
              <div className="flex items-center justify-between">
                <span className="font-label-md text-label-md uppercase tracking-wider text-text-secondary font-semibold">
                  Link Hoạt Động Trong Trang
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
                    / {metrics.pageCount} link
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
                      const shortUrl = getShortUrl(item);
                      const isActive = item.isActive;
                      const isExpired = getUrlStatus(item) === 'EXPIRED';

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
                              <span>{shortUrl}</span>
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
                              disabled={pendingStatusId !== null}
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
                                  navigator.clipboard.writeText(shortUrl)
                                    .then(() => triggerToast(`Đã sao chép: ${shortUrl}`))
                                    .catch(() => triggerToast('Không thể sao chép liên kết', 'Lỗi', 'error'));
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
                                    code: displayCode,
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

      <CreateUrlModal isOpen={isCreateModalOpen} form={createForm} onChange={setCreateForm}
        onClose={() => setIsCreateModalOpen(false)} onSubmit={handleCreateSubmit} isSubmitting={isCreating} />

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
