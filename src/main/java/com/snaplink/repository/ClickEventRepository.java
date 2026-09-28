package com.snaplink.repository;

import com.snaplink.entity.ClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {

    long countByUrlId(Long urlId);

    @Query("SELECT COUNT(DISTINCT c.ipAddress) FROM ClickEvent c WHERE c.url.id = :urlId AND c.ipAddress IS NOT NULL")
    long countDistinctIpByUrlId(@Param("urlId") Long urlId);

    List<ClickEvent> findByUrlIdOrderByClickedAtDesc(Long urlId);

    List<ClickEvent> findByUrlIdOrderByClickedAtAsc(Long urlId);

    List<ClickEvent> findByUrlIdAndClickedAtBetweenOrderByClickedAtAsc(Long urlId, Instant start, Instant end);

    @Query("SELECT c.deviceType, COUNT(c) FROM ClickEvent c WHERE c.url.id = :urlId GROUP BY c.deviceType ORDER BY COUNT(c) DESC")
    List<Object[]> countGroupedByDeviceType(@Param("urlId") Long urlId);

    @Query("SELECT c.browser, COUNT(c) FROM ClickEvent c WHERE c.url.id = :urlId GROUP BY c.browser ORDER BY COUNT(c) DESC")
    List<Object[]> countGroupedByBrowser(@Param("urlId") Long urlId);

    @Query("SELECT c.country, COUNT(c) FROM ClickEvent c WHERE c.url.id = :urlId GROUP BY c.country ORDER BY COUNT(c) DESC")
    List<Object[]> countGroupedByCountry(@Param("urlId") Long urlId);

    @Query("SELECT c.referrer, COUNT(c) FROM ClickEvent c WHERE c.url.id = :urlId AND c.referrer IS NOT NULL AND c.referrer <> '' GROUP BY c.referrer ORDER BY COUNT(c) DESC")
    List<Object[]> countGroupedByReferrer(@Param("urlId") Long urlId);
}
