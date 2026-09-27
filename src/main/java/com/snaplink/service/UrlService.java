package com.snaplink.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.snaplink.config.security.UserPrincipal;
import com.snaplink.dto.internal.UrlRedirectDto;
import com.snaplink.dto.request.CreateUrlRequest;
import com.snaplink.dto.response.PageResponse;
import com.snaplink.dto.response.UrlResponse;
import com.snaplink.entity.Url;
import com.snaplink.entity.User;
import com.snaplink.exception.BadRequestException;
import com.snaplink.exception.ConflictException;
import com.snaplink.exception.ResourceNotFoundException;
import com.snaplink.repository.UrlRepository;
import com.snaplink.repository.UserRepository;
import com.snaplink.util.Base62;
import com.snaplink.util.UrlValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository urlRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final Cache<String, UrlRedirectDto> urlCaffeineCache;

    private static final String REDIS_KEY_PREFIX = "url:code:";
    private static final Duration DEFAULT_CACHE_TTL = Duration.ofHours(24);

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Transactional
    public UrlResponse createShortUrl(CreateUrlRequest request, UserPrincipal currentUser) {
        UrlValidator.validateOriginalUrl(request.getOriginalUrl());

        if (request.getExpiresAt() != null && request.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Expiration date must be in the future");
        }

        String customAlias = (request.getCustomAlias() != null && !request.getCustomAlias().trim().isEmpty())
                ? request.getCustomAlias().trim()
                : null;

        if (customAlias != null) {
            if (currentUser == null) {
                throw new BadRequestException("Custom alias is only available for registered users");
            }

            UrlValidator.validateCustomAlias(customAlias);

            if (urlRepository.existsByCustomAlias(customAlias) || urlRepository.existsByShortCode(customAlias)) {
                throw new ConflictException("Custom alias '" + customAlias + "' is already taken");
            }
        }

        User user = null;
        if (currentUser != null) {
            user = userRepository.getReferenceById(currentUser.getId());
        }

        String tempShortCode = (customAlias != null)
                ? customAlias
                : UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        Url url = Url.builder()
                .originalUrl(request.getOriginalUrl().trim())
                .customAlias(customAlias)
                .shortCode(tempShortCode)
                .expiresAt(request.getExpiresAt())
                .user(user)
                .isActive(true)
                .build();

        try {
            Url savedUrl = urlRepository.save(url);

            if (customAlias == null) {
                String generatedCode = Base62.encode(savedUrl.getId() + Base62.BASE_OFFSET);
                savedUrl.setShortCode(generatedCode);
            }

            return mapToResponse(savedUrl);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Alias or short code already exists, please try another one.");
        }
    }

    /**
     * Two-Level Cache-Aside Redirect Lookup:
     * 1. L1 Check: In-Memory Caffeine Cache (< 1ms)
     * 2. L2 Check: Distributed Redis Cache (< 30ms) -> On hit, populate L1
     * 3. L3 Fallback: Query PostgreSQL DB -> Write-back to both L1 & L2 (with Smart TTL)
     * 4. Resilient: If Redis is down, gracefully serves from L1 and DB
     */
    @Transactional(readOnly = true)
    public String getOriginalUrl(String code) {
        String cacheKey = REDIS_KEY_PREFIX + code;

        // 1. Kiểm tra L1: In-Memory Caffeine Cache
        UrlRedirectDto l1Dto = urlCaffeineCache.getIfPresent(code);
        if (l1Dto != null) {
            log.debug("L1 Caffeine Cache HIT for code '{}'", code);
            validateRedirect(l1Dto.getIsActive(), l1Dto.getExpiresAt(), code, cacheKey);
            return l1Dto.getOriginalUrl();
        }

        // 2. Kiểm tra L2: Redis Cache
        UrlRedirectDto cachedDto = null;
        try {
            Object cachedObj = redisTemplate.opsForValue().get(cacheKey);
            if (cachedObj instanceof UrlRedirectDto dto) {
                cachedDto = dto;
            }
        } catch (Exception ex) {
            log.warn("Redis read error for key {}: {}. Falling back to DB.", cacheKey, ex.getMessage());
        }

        if (cachedDto != null) {
            log.debug("L2 Redis Cache HIT for code '{}'", code);
            validateRedirect(cachedDto.getIsActive(), cachedDto.getExpiresAt(), code, cacheKey);
            // Nạp vào L1 Caffeine để các request kế tiếp truy cập siêu tốc
            urlCaffeineCache.put(code, cachedDto);
            return cachedDto.getOriginalUrl();
        }

        // 3. Cache MISS toàn bộ: Truy vấn PostgreSQL DB
        log.debug("Cache MISS for code '{}'. Querying Database.", code);
        Url url = urlRepository.findByShortCode(code)
                .or(() -> urlRepository.findByCustomAlias(code))
                .orElseThrow(() -> new ResourceNotFoundException("Short URL not found: " + code));

        // 4. Kiểm tra tính hợp lệ
        validateRedirect(url.getIsActive(), url.getExpiresAt(), code, null);

        UrlRedirectDto redirectDto = UrlRedirectDto.builder()
                .id(url.getId())
                .originalUrl(url.getOriginalUrl())
                .shortCode(url.getShortCode())
                .expiresAt(url.getExpiresAt())
                .isActive(url.getIsActive())
                .build();

        // 5. Ghi dữ liệu vào L1 Caffeine Cache
        urlCaffeineCache.put(code, redirectDto);

        // 6. Ghi dữ liệu vào L2 Redis Cache với Smart TTL
        Duration ttl = calculateTtl(url.getExpiresAt());
        try {
            redisTemplate.opsForValue().set(cacheKey, redirectDto, ttl);
            log.debug("Cached code '{}' in Redis with Smart TTL {}s", code, ttl.getSeconds());
        } catch (Exception ex) {
            log.warn("Redis write error for key {}: {}. System operational via L1 & DB.", cacheKey, ex.getMessage());
        }

        return url.getOriginalUrl();
    }

    @Transactional(readOnly = true)
    public PageResponse<UrlResponse> getMyUrls(UserPrincipal currentUser, Pageable pageable) {
        Page<Url> page = urlRepository.findByUserId(currentUser.getId(), pageable);
        return PageResponse.from(page, this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public UrlResponse getUrlById(Long id, UserPrincipal currentUser) {
        Url url = urlRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("URL not found with id: " + id));

        return mapToResponse(url);
    }

    @Transactional
    public void deleteUrl(Long id, UserPrincipal currentUser) {
        Url url = urlRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("URL not found with id: " + id));

        // Invalidate cả L1 Caffeine và L2 Redis
        evictCache(url.getShortCode(), url.getCustomAlias());
        urlRepository.delete(url);
    }

    @Transactional
    public UrlResponse toggleUrlStatus(Long id, UserPrincipal currentUser) {
        Url url = urlRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("URL not found with id: " + id));

        url.setIsActive(!Boolean.TRUE.equals(url.getIsActive()));
        Url updatedUrl = urlRepository.save(url);

        // Invalidate cả L1 Caffeine và L2 Redis
        evictCache(url.getShortCode(), url.getCustomAlias());

        return mapToResponse(updatedUrl);
    }

    private void validateRedirect(Boolean isActive, Instant expiresAt, String code, String cacheKey) {
        if (!Boolean.TRUE.equals(isActive)) {
            urlCaffeineCache.invalidate(code);
            if (cacheKey != null) {
                try {
                    redisTemplate.delete(cacheKey);
                } catch (Exception ignored) {}
            }
            throw new ResourceNotFoundException("Link has been disabled: " + code);
        }

        if (expiresAt != null && expiresAt.isBefore(Instant.now())) {
            urlCaffeineCache.invalidate(code);
            if (cacheKey != null) {
                try {
                    redisTemplate.delete(cacheKey);
                } catch (Exception ignored) {}
            }
            throw new ResourceNotFoundException("Link has expired: " + code);
        }
    }

    private Duration calculateTtl(Instant expiresAt) {
        if (expiresAt == null) {
            return DEFAULT_CACHE_TTL;
        }
        Duration remaining = Duration.between(Instant.now(), expiresAt);
        if (remaining.isNegative() || remaining.isZero()) {
            return Duration.ofSeconds(1);
        }
        return remaining.compareTo(DEFAULT_CACHE_TTL) < 0 ? remaining : DEFAULT_CACHE_TTL;
    }

    private void evictCache(String shortCode, String customAlias) {
        // 1. Evict L1 Caffeine
        if (shortCode != null) {
            urlCaffeineCache.invalidate(shortCode);
        }
        if (customAlias != null) {
            urlCaffeineCache.invalidate(customAlias);
        }

        // 2. Evict L2 Redis
        try {
            if (shortCode != null) {
                redisTemplate.delete(REDIS_KEY_PREFIX + shortCode);
            }
            if (customAlias != null && !customAlias.equals(shortCode)) {
                redisTemplate.delete(REDIS_KEY_PREFIX + customAlias);
            }
        } catch (Exception ex) {
            log.warn("Failed to evict Redis cache: {}", ex.getMessage());
        }
    }

    private UrlResponse mapToResponse(Url url) {
        String code = url.getShortCode();
        String fullShortUrl = (baseUrl.endsWith("/") ? baseUrl : baseUrl + "/") + code;

        return UrlResponse.builder()
                .id(url.getId())
                .shortCode(url.getShortCode())
                .shortUrl(fullShortUrl)
                .originalUrl(url.getOriginalUrl())
                .customAlias(url.getCustomAlias())
                .expiresAt(url.getExpiresAt())
                .createdAt(url.getCreatedAt())
                .isActive(url.getIsActive())
                .userId(url.getUser() != null ? url.getUser().getId() : null)
                .build();
    }
}