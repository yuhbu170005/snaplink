package com.snaplink.repository;

import com.snaplink.entity.ClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/** Truy vấn và thống kê click tại database; @Query dùng khi tên hàm không diễn tả đủ truy vấn. */
@Repository
public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {

    long countByUrlId(Long urlId);

    @Query("SELECT COUNT(DISTINCT c.ipAddress) FROM ClickEvent c WHERE c.url.id = :urlId AND c.ipAddress IS NOT NULL")
    long countDistinctIpByUrlId(@Param("urlId") Long urlId);

    /** DB gom theo ngày UTC; không tải toàn bộ click về JVM để đếm. */
    @Query(value = """
            SELECT CAST(clicked_at AT TIME ZONE 'UTC' AS DATE), COUNT(*)
            FROM click_events WHERE url_id = :urlId
            GROUP BY CAST(clicked_at AT TIME ZONE 'UTC' AS DATE)
            ORDER BY CAST(clicked_at AT TIME ZONE 'UTC' AS DATE)
            """, nativeQuery = true)
    List<Object[]> countGroupedByDay(@Param("urlId") Long urlId);

    @Query("SELECT c.deviceType, COUNT(c) FROM ClickEvent c WHERE c.url.id = :urlId GROUP BY c.deviceType ORDER BY COUNT(c) DESC")
    List<Object[]> countGroupedByDeviceType(@Param("urlId") Long urlId);

    @Query("SELECT c.browser, COUNT(c) FROM ClickEvent c WHERE c.url.id = :urlId GROUP BY c.browser ORDER BY COUNT(c) DESC")
    List<Object[]> countGroupedByBrowser(@Param("urlId") Long urlId);

    @Query("SELECT c.country, COUNT(c) FROM ClickEvent c WHERE c.url.id = :urlId GROUP BY c.country ORDER BY COUNT(c) DESC")
    List<Object[]> countGroupedByCountry(@Param("urlId") Long urlId);

    @Query("SELECT COALESCE(NULLIF(c.referrer, ''), 'Direct'), COUNT(c) FROM ClickEvent c WHERE c.url.id = :urlId GROUP BY COALESCE(NULLIF(c.referrer, ''), 'Direct') ORDER BY COUNT(c) DESC")
    List<Object[]> countGroupedByReferrer(@Param("urlId") Long urlId);
}
