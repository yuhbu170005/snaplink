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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** Nghiệp vụ link: kiểm tra đầu vào, sinh mã, kiểm tra chủ sở hữu và tra cache khi redirect. */
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

    /** Tạo link cho guest hoặc user; chỉ user đã đăng nhập mới được chọn custom alias. */
    @Transactional
    public UrlResponse createShortUrl(CreateUrlRequest request, UserPrincipal currentUser) {
        UrlValidator.validateOriginalUrl(request.getOriginalUrl());

        if (request.getExpiresAt() != null && !request.getExpiresAt().isAfter(Instant.now())) {
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
                // Alias do user chọn có thể trùng mã Base62 của một ID được tạo sau đó.
                // ID tiếp theo vẫn duy nhất; thêm '_' cho đến khi có routing key còn trống.
                while (urlRepository.existsByShortCode(generatedCode)
                        || urlRepository.existsByCustomAlias(generatedCode)) {
                    generatedCode = "_" + generatedCode;
                }
                savedUrl.setShortCode(generatedCode);
            }

            // JPA thường chỉ UPDATE lúc commit. Flush ngay để bắt lỗi UNIQUE tại đây.
            urlRepository.flush();
            return mapToResponse(savedUrl);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Alias or short code already exists, please try another one.");
        }
    }

    /** Hàm tiện ích lấy URL gốc từ cùng luồng kiểm tra redirect. */
    @Transactional(readOnly = true)
    public String getOriginalUrl(String code) {
        return getRedirectInfo(code).getOriginalUrl();
    }

    /**
     * Cache-aside: đọc Caffeine (RAM), rồi Redis, cuối cùng mới đọc database.
     * Redis chỉ giúp tăng tốc; lỗi Redis không được làm hỏng redirect.
     */
    @Transactional(readOnly = true)
    public UrlRedirectDto getRedirectInfo(String code) {
        UrlRedirectDto redirect = urlCaffeineCache.getIfPresent(code);
        if (redirect != null) {
            validateRedirect(redirect, code);
            return redirect; // Không put lại: cache hit không được kéo dài TTL của L1.
        }
        redirect = readRedis(code);
        if (redirect == null) {
            Url url = urlRepository.findByShortCode(code)
                    .or(() -> urlRepository.findByCustomAlias(code))
                    .orElseThrow(() -> new ResourceNotFoundException("Short URL not found: " + code));
            redirect = UrlRedirectDto.builder()
                    .id(url.getId())
                    .originalUrl(url.getOriginalUrl())
                    .shortCode(url.getShortCode())
                    .expiresAt(url.getExpiresAt())
                    .isActive(url.getIsActive())
                    .build();
            validateRedirect(redirect, code);
            writeRedis(code, redirect);
        }

        // Cache hit vẫn phải kiểm tra expiration; TTL của Caffeine không phải hạn dùng link.
        validateRedirect(redirect, code);
        urlCaffeineCache.put(code, redirect);
        return redirect;
    }

    /** Đọc Redis an toàn. Trả null nghĩa là cache miss hoặc Redis đang lỗi. */
    private UrlRedirectDto readRedis(String code) {
        try {
            Object value = redisTemplate.opsForValue().get(REDIS_KEY_PREFIX + code);
            return value instanceof UrlRedirectDto dto ? dto : null;
        } catch (Exception ex) {
            log.warn("Cannot read redirect cache: {}", ex.getMessage());
            return null;
        }
    }

    /** Cache không sống lâu hơn link và tối đa 24 giờ. */
    private void writeRedis(String code, UrlRedirectDto redirect) {
        try {
            redisTemplate.opsForValue().set(REDIS_KEY_PREFIX + code, redirect, calculateTtl(redirect.getExpiresAt()));
        } catch (Exception ex) {
            log.warn("Cannot write redirect cache: {}", ex.getMessage());
        }
    }

    /** Chỉ đọc link của user hiện tại, có phân trang để giới hạn dữ liệu mỗi request. */
    @Transactional(readOnly = true)
    public PageResponse<UrlResponse> getMyUrls(UserPrincipal currentUser, Pageable pageable) {
        Page<Url> page = urlRepository.findByUserId(currentUser.getId(), pageable);
        return PageResponse.from(page, this::mapToResponse);
    }

    /** Đọc một link kèm điều kiện sở hữu; không cho phép đọc link của tài khoản khác. */
    @Transactional(readOnly = true)
    public UrlResponse getUrlById(Long id, UserPrincipal currentUser) {
        Url url = findOwnedUrl(id, currentUser);

        return mapToResponse(url);
    }

    /** Xoá link của chính user; click liên quan được xoá theo quan hệ và cache được vô hiệu hoá. */
    @Transactional
    public void deleteUrl(Long id, UserPrincipal currentUser) {
        Url url = findOwnedUrl(id, currentUser);

        // Invalidate cả L1 Caffeine và L2 Redis
        urlRepository.delete(url);
        evictAfterCommit(url);
    }

    /** Đảo isActive hiện tại; API này không nhận trạng thái đích trong body. */
    @Transactional
    public UrlResponse toggleUrlStatus(Long id, UserPrincipal currentUser) {
        Url url = findOwnedUrl(id, currentUser);

        url.setIsActive(!Boolean.TRUE.equals(url.getIsActive()));
        Url updatedUrl = urlRepository.save(url);

        // Invalidate cả L1 Caffeine và L2 Redis
        evictAfterCommit(url);

        return mapToResponse(updatedUrl);
    }

    /** Không trả dữ liệu của người khác; dùng 404 để không tiết lộ link có tồn tại hay không. */
    private Url findOwnedUrl(Long id, UserPrincipal currentUser) {
        return urlRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("URL not found with id: " + id));
    }

    private void validateRedirect(UrlRedirectDto redirect, String code) {
        boolean disabled = !Boolean.TRUE.equals(redirect.getIsActive());
        boolean expired = redirect.getExpiresAt() != null && !redirect.getExpiresAt().isAfter(Instant.now());
        if (disabled || expired) {
            evictCache(code, null);
            String reason = disabled ? "Link has been disabled: " : "Link has expired: ";
            throw new ResourceNotFoundException(reason + code);
        }
    }

    /**
     * Chỉ xoá cache sau khi DB commit thành công. Nếu xoá trước commit, request khác
     * có thể đọc bản cũ trong DB rồi nạp ngược lại cache.
     */
    private void evictAfterCommit(Url url) {
        String code = url.getShortCode();
        String alias = url.getCustomAlias();
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            evictCache(code, alias); // Unit test gọi service trực tiếp không có Spring transaction.
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                evictCache(code, alias);
            }
        });
    }

    /** TTL bằng thời gian còn lại của link, nhưng không quá giới hạn cache mặc định. */
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

    /** Xoá mã và alias khỏi Caffeine/Redis; lỗi cache không làm rollback nghiệp vụ đã thành công. */
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

    /** Chuyển entity sang DTO, tạo shortUrl bằng base URL của backend. */
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