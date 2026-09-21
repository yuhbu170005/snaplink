package com.snaplink.repository;

import com.snaplink.entity.ClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {
    long countByUrlId(Long urlId);
    List<ClickEvent> findByUrlIdOrderByClickedAtDesc(Long urlId);
}
