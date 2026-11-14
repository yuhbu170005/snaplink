-- Alias và mã tự sinh dùng chung short_code để redirect.
-- Giữ V1 nguyên vẹn cho database đã chạy Flyway; mở rộng bằng migration mới.
ALTER TABLE urls ALTER COLUMN short_code TYPE VARCHAR(64);
