package com.snaplink.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/** Entity ánh xạ một lượt truy cập. Mỗi click là một dòng mới, không UPDATE bộ đếm trong urls. */
@Entity
@Table(
    name = "click_events",
    indexes = {
        @Index(name = "idx_click_events_url_id", columnList = "url_id"),
        @Index(name = "idx_click_events_clicked_at", columnList = "clicked_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClickEvent {

    // IDENTITY: database cấp ID khi INSERT, không phải Java tự tăng biến.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // LAZY: chỉ tải entity liên quan khi cần, trong transaction của service.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "url_id", nullable = false)
    private Url url;

    // Giữ thời điểm request xảy ra, không ghi đè bằng thời điểm worker xử lý.
    @Builder.Default
    @Column(name = "clicked_at", nullable = false, updatable = false)
    private Instant clickedAt = Instant.now();

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(length = 100)
    private String country;

    @Column(name = "device_type", length = 50)
    private String deviceType;

    @Column(length = 50)
    private String browser;

    @Column(columnDefinition = "TEXT")
    private String referrer;
}
