import test from 'node:test';
import assert from 'node:assert/strict';
import { csvCell, createUrlPayload, getErrorMessage, getUrlStatus } from '../src/utils/urlUtils.js';
import { filterTimeSeries } from '../src/utils/analyticsUtils.js';

const now = new Date('2026-11-02T12:00:00Z');

test('isActive và expiration là hai điều kiện độc lập', () => {
  assert.equal(getUrlStatus({ isActive: true }, +now), 'ACTIVE');
  assert.equal(getUrlStatus({ isActive: false }, +now), 'INACTIVE');
  assert.equal(getUrlStatus({ isActive: true, expiresAt: '2026-11-02T12:00:00Z' }, +now), 'EXPIRED');
});

test('payload bỏ alias rỗng và chuyển thời điểm về UTC', () => {
  assert.deepEqual(createUrlPayload({ originalUrl: ' https://example.com ', customAlias: ' ', expiresAt: '' }), {
    originalUrl: 'https://example.com', customAlias: undefined, expiresAt: undefined,
  });
  assert.equal(createUrlPayload({ originalUrl: 'https://example.com', expiresAt: '2026-11-02T19:00:00+07:00' }).expiresAt, now.toISOString());
});

test('lỗi validation hiển thị chi tiết; lỗi mạng dùng fallback', () => {
  assert.equal(getErrorMessage({ response: { data: { message: 'Validation failed', errors: { email: 'Invalid email' } } } }, 'fallback'), 'Invalid email');
  assert.equal(getErrorMessage({}, 'Mất kết nối'), 'Mất kết nối');
});

test('CSV escape quote/newline và không thực thi công thức', () => {
  assert.equal(csvCell('a,"b"\nc'), '"a,""b""\nc"');
  assert.equal(csvCell('=HYPERLINK("x")'), '"\'=HYPERLINK(""x"")"');
  assert.equal(csvCell(' @SUM(A1:A2)'), '"\' @SUM(A1:A2)"');
});

test('lọc 7/30 ngày dùng UTC, bao gồm ngày đầu và loại ngày tương lai', () => {
  const points = ['2026-10-26', '2026-10-27', '2026-11-02', '2026-11-03'].map((timestamp) => ({ timestamp, clicks: 0 }));
  assert.deepEqual(filterTimeSeries(points, '7', now).map((point) => point.timestamp), ['2026-10-27', '2026-11-02']);
  assert.equal(filterTimeSeries(points, '30', now).length, 3);
  assert.deepEqual(filterTimeSeries([], '7', now), []);
  assert.equal(filterTimeSeries(points, 'all', now), points);
});
