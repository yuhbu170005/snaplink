package com.snaplink.entity;

import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Entity ánh xạ bảng urls. user có thể null vì guest cũng được phép tạo link. */
@Entity
@Table(
    name = "urls",
    indexes = {
        @Index(name = "idx_urls_short_code", columnList = "short_code"),
        @Index(name = "idx_urls_user_id", columnList = "user_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Url {

    // IDENTITY: database cấp ID khi INSERT, không phải Java tự tăng biến.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "short_code", nullable = false, unique = true, length = 64)
    private String shortCode;

    @Column(name = "original_url", nullable = false, columnDefinition = "TEXT")
    private String originalUrl;

    @Column(name = "custom_alias", unique = true, length = 64)
    private String customAlias;

    // LAZY: chỉ tải entity liên quan khi cần, trong transaction của service.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // Quan hệ ngược: mappedBy trỏ tới field phía entity con, không tạo thêm cột ở đây.
    @OneToMany(mappedBy = "url", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ClickEvent> clickEvents = new ArrayList<>();
}
