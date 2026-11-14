/** Backend trả isActive (boolean); hết hạn là trạng thái suy ra từ expiresAt. */
export function getUrlStatus(url, now = Date.now()) {
  if (url.expiresAt && new Date(url.expiresAt).getTime() <= now) return 'EXPIRED';
  return url.isActive ? 'ACTIVE' : 'INACTIVE';
}

/** Dùng URL backend trả về, tránh hard-code domain demo thành link thật. */
export function getShortUrl(url) {
  return url.shortUrl;
}

/** datetime-local dùng giờ máy người dùng; API nhận ISO UTC. */
export function createUrlPayload({ originalUrl, customAlias, expiresAt }) {
  return {
    originalUrl: originalUrl.trim(),
    customAlias: customAlias?.trim() || undefined,
    expiresAt: expiresAt ? new Date(expiresAt).toISOString() : undefined,
  };
}

/** Ưu tiên lỗi validation cụ thể, sau đó mới dùng thông báo chung từ backend. */
export function getErrorMessage(error, fallback) {
  const data = error.response?.data;
  const validationMessages = Object.values(data?.errors || {});
  return validationMessages.length ? validationMessages.join('. ') : data?.message || fallback;
}

/** Escape cả dấu phẩy, dấu nháy và chặn công thức khi mở CSV bằng spreadsheet. */
export function csvCell(value) {
  let text = String(value ?? '');
  if (/^[\s]*[=+\-@]/.test(text)) text = `'${text}`;
  return `"${text.replaceAll('"', '""')}"`;
}
