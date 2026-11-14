/** Component thuần hiển thị: nhận dữ liệu từ trang, không tự gọi API. */
export function MetricCard({ title, value, description }) {
  return (
    <section className="p-5 rounded-2xl bg-surface-card border border-border-subtle shadow-sm">
      <h2 className="text-text-secondary text-sm">{title}</h2>
      <p className="text-3xl font-bold text-text-primary mt-3">{value}</p>
      {description && <p className="text-sm text-text-muted mt-2">{description}</p>}
    </section>
  );
}

export function StatsList({ title, items = [] }) {
  return (
    <section className="p-6 rounded-2xl bg-surface-card border border-border-subtle shadow-sm">
      <h2 className="text-xl font-bold text-text-primary mb-4">{title}</h2>
      {!items.length && <p className="text-text-muted">Chưa có dữ liệu.</p>}
      <ul className="space-y-4">
        {items.map((item) => (
          <li key={item.name}>
            <div className="flex justify-between gap-4 text-sm">
              <span className="break-all">{item.name === 'UNKNOWN' ? 'Chưa xác định' : item.name === 'LOCAL' ? 'Mạng nội bộ' : item.name}</span>
              <span className="shrink-0">{item.count.toLocaleString()} ({item.percentage}%)</span>
            </div>
            <div className="h-2 mt-2 rounded bg-surface-container overflow-hidden">
              <div className="h-full bg-brand-violet" style={{ width: `${item.percentage}%` }} />
            </div>
          </li>
        ))}
      </ul>
    </section>
  );
}
