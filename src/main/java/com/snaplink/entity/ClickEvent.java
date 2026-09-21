package com.snaplink.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

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
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClickEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "url_id", nullable = false)
    Url url;

    @CreationTimestamp
    @Column(name = "clicked_at", nullable = false, updatable = false)
    Instant clickedAt;

    @Column(name = "ip_address", length = 45)
    String ipAddress;

    @Column(length = 100)
    String country;

    @Column(name = "device_type", length = 50)
    String deviceType;

    @Column(length = 50)
    String browser;

    @Column(columnDefinition = "TEXT")
    String referrer;
}
