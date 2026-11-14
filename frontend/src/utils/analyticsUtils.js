/** Cùng dùng ngày UTC với backend để không lệch ngày khi người xem ở múi giờ khác. */
export function filterTimeSeries(points, range, now = new Date()) {
  if (range === 'all') return points;
  const cutoff = new Date(now);
  cutoff.setUTCDate(cutoff.getUTCDate() - Number(range) + 1);
  const firstDay = cutoff.toISOString().slice(0, 10);
  const lastDay = now.toISOString().slice(0, 10);
  return points.filter((point) => point.timestamp >= firstDay && point.timestamp <= lastDay);
}
