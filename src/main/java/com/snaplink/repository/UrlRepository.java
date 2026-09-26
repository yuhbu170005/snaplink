package com.snaplink.repository;

import com.snaplink.entity.Url;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {
    Optional<Url> findByShortCode(String shortCode);
    Optional<Url> findByCustomAlias(String customAlias);
    boolean existsByShortCode(String shortCode);
    boolean existsByCustomAlias(String customAlias);
    List<Url> findByUserIdOrderByCreatedAtDesc(Long userId);
    Page<Url> findByUserId(Long userId, Pageable pageable);
    Optional<Url> findByIdAndUserId(Long id, Long userId);
}
