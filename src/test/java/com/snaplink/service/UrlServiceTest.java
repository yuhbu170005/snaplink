package com.snaplink.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private Cache<String, UrlRedirectDto> urlCaffeineCache;

    @InjectMocks
    private UrlService urlService;

    private User sampleUser;
    private UserPrincipal samplePrincipal;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(urlService, "baseUrl", "http://localhost:8080");

        urlCaffeineCache = Caffeine.newBuilder().maximumSize(100).build();
        ReflectionTestUtils.setField(urlService, "urlCaffeineCache", urlCaffeineCache);

        sampleUser = User.builder()
                .id(1L)
                .email("user@snaplink.com")
                .passwordHash("hashed_pwd")
                .build();

        samplePrincipal = new UserPrincipal(1L, "user@snaplink.com", "hashed_pwd", Collections.emptyList());
    }

    @Test
    @DisplayName("createShortUrl: thành công cho Guest User (không cần đăng nhập)")
    void createShortUrl_Guest_Success() {
        CreateUrlRequest request = CreateUrlRequest.builder()
                .originalUrl("https://spring.io/projects/spring-boot")
                .build();

        Url savedInitial = Url.builder()
                .id(100L)
                .originalUrl("https://spring.io/projects/spring-boot")
                .shortCode("temp")
                .isActive(true)
                .createdAt(Instant.now())
                .build();

        when(urlRepository.save(any(Url.class))).thenReturn(savedInitial);

        UrlResponse response = urlService.createShortUrl(request, null);

        assertThat(response).isNotNull();
        assertThat(response.getOriginalUrl()).isEqualTo("https://spring.io/projects/spring-boot");
        assertThat(response.getShortUrl()).startsWith("http://localhost:8080/");
        assertThat(response.getUserId()).isNull();
        verify(urlRepository, times(1)).save(any(Url.class));
    }

    @Test
    @DisplayName("createShortUrl: thành công cho User đã đăng nhập")
    void createShortUrl_AuthenticatedUser_Success() {
        CreateUrlRequest request = CreateUrlRequest.builder()
                .originalUrl("https://github.com/snaplink")
                .build();

        Url savedUrl = Url.builder()
                .id(101L)
                .originalUrl("https://github.com/snaplink")
                .shortCode("abc12")
                .user(sampleUser)
                .isActive(true)
                .createdAt(Instant.now())
                .build();

        when(userRepository.getReferenceById(1L)).thenReturn(sampleUser);
        when(urlRepository.save(any(Url.class))).thenReturn(savedUrl);

        UrlResponse response = urlService.createShortUrl(request, samplePrincipal);

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("createShortUrl: thành công với Custom Alias khi đã đăng nhập")
    void createShortUrl_WithCustomAlias_Success() {
        CreateUrlRequest request = CreateUrlRequest.builder()
                .originalUrl("https://linkedin.com/in/ngankim")
                .customAlias("ngan-profile")
                .build();

        Url savedUrl = Url.builder()
                .id(102L)
                .originalUrl("https://linkedin.com/in/ngankim")
                .customAlias("ngan-profile")
                .shortCode("ngan-profile")
                .user(sampleUser)
                .isActive(true)
                .createdAt(Instant.now())
                .build();

        when(userRepository.getReferenceById(1L)).thenReturn(sampleUser);
        when(urlRepository.existsByCustomAlias("ngan-profile")).thenReturn(false);
        when(urlRepository.existsByShortCode("ngan-profile")).thenReturn(false);
        when(urlRepository.save(any(Url.class))).thenReturn(savedUrl);

        UrlResponse response = urlService.createShortUrl(request, samplePrincipal);

        assertThat(response).isNotNull();
        assertThat(response.getCustomAlias()).isEqualTo("ngan-profile");
        assertThat(response.getShortUrl()).isEqualTo("http://localhost:8080/ngan-profile");
    }

    @Test
    @DisplayName("createShortUrl: ném lỗi khi Guest User gửi Custom Alias")
    void createShortUrl_Guest_WithCustomAlias_ThrowsBadRequest() {
        CreateUrlRequest request = CreateUrlRequest.builder()
                .originalUrl("https://example.com")
                .customAlias("my-link")
                .build();

        assertThatThrownBy(() -> urlService.createShortUrl(request, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Custom alias is only available for registered users");

        verify(urlRepository, never()).save(any(Url.class));
    }

    @Test
    @DisplayName("createShortUrl: ném ConflictException khi Custom Alias bị trùng lặp")
    void createShortUrl_DuplicateCustomAlias_ThrowsConflict() {
        CreateUrlRequest request = CreateUrlRequest.builder()
                .originalUrl("https://example.com")
                .customAlias("existing-link")
                .build();

        when(urlRepository.existsByCustomAlias("existing-link")).thenReturn(true);

        assertThatThrownBy(() -> urlService.createShortUrl(request, samplePrincipal))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already taken");

        verify(urlRepository, never()).save(any(Url.class));
    }

    @Test
    @DisplayName("createShortUrl: ném ConflictException khi Database ném DataIntegrityViolationException")
    void createShortUrl_DbConstraintViolation_ThrowsConflict() {
        CreateUrlRequest request = CreateUrlRequest.builder()
                .originalUrl("https://example.com")
                .customAlias("race-condition-alias")
                .build();

        when(userRepository.getReferenceById(1L)).thenReturn(sampleUser);
        when(urlRepository.existsByCustomAlias("race-condition-alias")).thenReturn(false);
        when(urlRepository.existsByShortCode("race-condition-alias")).thenReturn(false);
        when(urlRepository.save(any(Url.class))).thenThrow(new DataIntegrityViolationException("Duplicate key error"));

        assertThatThrownBy(() -> urlService.createShortUrl(request, samplePrincipal))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("createShortUrl: ném lỗi khi ngày hết hạn trong quá khứ")
    void createShortUrl_PastExpiresAt_ThrowsBadRequest() {
        CreateUrlRequest request = CreateUrlRequest.builder()
                .originalUrl("https://example.com")
                .expiresAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .build();

        assertThatThrownBy(() -> urlService.createShortUrl(request, samplePrincipal))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Expiration date must be in the future");
    }

    @Test
    @DisplayName("getOriginalUrl: L1 Caffeine Cache HIT -> trả về URL gốc siêu tốc mà không query Redis hay DB")
    void getOriginalUrl_L1CaffeineHit_ReturnsOriginalUrlImmediately() {
        UrlRedirectDto l1Dto = UrlRedirectDto.builder()
                .id(1L)
                .shortCode("l1Code")
                .originalUrl("https://l1-fast.com")
                .isActive(true)
                .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .build();

        urlCaffeineCache.put("l1Code", l1Dto);

        String originalUrl = urlService.getOriginalUrl("l1Code");

        assertThat(originalUrl).isEqualTo("https://l1-fast.com");
        verifyNoInteractions(redisTemplate);
        verify(urlRepository, never()).findByShortCode(anyString());
    }

    @Test
    @DisplayName("getOriginalUrl: L2 Redis HIT -> nạp vào L1 Caffeine và trả về URL gốc")
    void getOriginalUrl_L2RedisHit_PopulatesL1AndReturnsOriginalUrl() {
        UrlRedirectDto redisDto = UrlRedirectDto.builder()
                .id(1L)
                .shortCode("redisHitCode")
                .originalUrl("https://redis-target.com")
                .isActive(true)
                .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .build();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("url:code:redisHitCode")).thenReturn(redisDto);

        String originalUrl = urlService.getOriginalUrl("redisHitCode");

        assertThat(originalUrl).isEqualTo("https://redis-target.com");
        // Verify L1 was populated
        assertThat(urlCaffeineCache.getIfPresent("redisHitCode")).isNotNull();
        assertThat(urlCaffeineCache.getIfPresent("redisHitCode").getOriginalUrl()).isEqualTo("https://redis-target.com");
        verify(urlRepository, never()).findByShortCode(anyString());
    }

    @Test
    @DisplayName("getOriginalUrl: Redis Exception -> Graceful Degradation phục vụ từ DB và ghi vào L1")
    void getOriginalUrl_RedisDown_GracefullyServesFromDbAndCachesInL1() {
        Url dbUrl = Url.builder()
                .id(5L)
                .shortCode("redisDownCode")
                .originalUrl("https://resilient-system.com")
                .isActive(true)
                .expiresAt(null)
                .build();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("url:code:redisDownCode")).thenThrow(new RuntimeException("Redis connection refused"));
        doThrow(new RuntimeException("Redis write timeout")).when(valueOperations).set(anyString(), any(), any(Duration.class));
        when(urlRepository.findByShortCode("redisDownCode")).thenReturn(Optional.of(dbUrl));

        String originalUrl = urlService.getOriginalUrl("redisDownCode");

        assertThat(originalUrl).isEqualTo("https://resilient-system.com");
        assertThat(urlCaffeineCache.getIfPresent("redisDownCode")).isNotNull();
        assertThat(urlCaffeineCache.getIfPresent("redisDownCode").getOriginalUrl()).isEqualTo("https://resilient-system.com");
    }

    @Test
    @DisplayName("getOriginalUrl: Cache MISS -> query Database, lưu vào cả L1 Caffeine và L2 Redis (với Smart TTL)")
    void getOriginalUrl_CacheMiss_QueriesDbAndWritesToBothCaches() {
        Url dbUrl = Url.builder()
                .id(2L)
                .shortCode("missCode")
                .originalUrl("https://db-target.com")
                .isActive(true)
                .expiresAt(Instant.now().plus(2, ChronoUnit.DAYS))
                .build();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("url:code:missCode")).thenReturn(null);
        when(urlRepository.findByShortCode("missCode")).thenReturn(Optional.of(dbUrl));

        String originalUrl = urlService.getOriginalUrl("missCode");

        assertThat(originalUrl).isEqualTo("https://db-target.com");
        verify(urlRepository, times(1)).findByShortCode("missCode");
        verify(valueOperations, times(1)).set(eq("url:code:missCode"), any(UrlRedirectDto.class), any(Duration.class));
    }

    @Test
    @DisplayName("getOriginalUrl: ném ResourceNotFoundException khi link không tồn tại cả ở Cache và DB")
    void getOriginalUrl_NotFound_ThrowsResourceNotFoundException() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("url:code:invalidCode")).thenReturn(null);
        when(urlRepository.findByShortCode("invalidCode")).thenReturn(Optional.empty());
        when(urlRepository.findByCustomAlias("invalidCode")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> urlService.getOriginalUrl("invalidCode"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Short URL not found: invalidCode");
    }

    @Test
    @DisplayName("getOriginalUrl: ném ResourceNotFoundException khi link đã bị vô hiệu hoá (isActive = false)")
    void getOriginalUrl_DisabledLink_ThrowsResourceNotFoundException() {
        Url dbUrl = Url.builder()
                .id(3L)
                .shortCode("disabledCode")
                .originalUrl("https://disabled.com")
                .isActive(false)
                .build();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("url:code:disabledCode")).thenReturn(null);
        when(urlRepository.findByShortCode("disabledCode")).thenReturn(Optional.of(dbUrl));

        assertThatThrownBy(() -> urlService.getOriginalUrl("disabledCode"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Link has been disabled: disabledCode");
    }

    @Test
    @DisplayName("getOriginalUrl: ném ResourceNotFoundException và xoá cache khi link đã hết hạn")
    void getOriginalUrl_ExpiredLink_ThrowsResourceNotFoundException() {
        UrlRedirectDto expiredDto = UrlRedirectDto.builder()
                .id(4L)
                .shortCode("expiredCode")
                .originalUrl("https://expired.com")
                .isActive(true)
                .expiresAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .build();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("url:code:expiredCode")).thenReturn(expiredDto);

        assertThatThrownBy(() -> urlService.getOriginalUrl("expiredCode"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Link has expired: expiredCode");

        verify(redisTemplate, times(1)).delete("url:code:expiredCode");
    }

    @Test
    @DisplayName("getMyUrls: trả về danh sách phân trang đúng cho user")
    void getMyUrls_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Url url1 = Url.builder().id(1L).shortCode("code1").originalUrl("https://link1.com").user(sampleUser).isActive(true).createdAt(Instant.now()).build();
        Page<Url> page = new PageImpl<>(List.of(url1), pageable, 1);

        when(urlRepository.findByUserId(1L, pageable)).thenReturn(page);

        PageResponse<UrlResponse> response = urlService.getMyUrls(samplePrincipal, pageable);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getShortCode()).isEqualTo("code1");
        assertThat(response.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("getUrlById: thành công khi link thuộc sở hữu của user")
    void getUrlById_Success() {
        Url url = Url.builder().id(10L).shortCode("code10").originalUrl("https://link10.com").user(sampleUser).isActive(true).createdAt(Instant.now()).build();
        when(urlRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(url));

        UrlResponse response = urlService.getUrlById(10L, samplePrincipal);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getShortCode()).isEqualTo("code10");
    }

    @Test
    @DisplayName("getUrlById: ném ResourceNotFoundException khi link không thuộc về user")
    void getUrlById_NotFound_ThrowsResourceNotFound() {
        when(urlRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> urlService.getUrlById(99L, samplePrincipal))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("URL not found with id: 99");
    }

    @Test
    @DisplayName("deleteUrl: xoá thành công và evict cache khỏi Redis")
    void deleteUrl_Success() {
        Url url = Url.builder().id(10L).shortCode("code10").customAlias("custom10").originalUrl("https://link10.com").user(sampleUser).build();
        when(urlRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(url));

        urlService.deleteUrl(10L, samplePrincipal);

        verify(urlRepository, times(1)).delete(url);
        verify(redisTemplate, times(1)).delete("url:code:code10");
        verify(redisTemplate, times(1)).delete("url:code:custom10");
    }

    @Test
    @DisplayName("deleteUrl: ném ResourceNotFoundException khi link không tồn tại hoặc không phải của user")
    void deleteUrl_NotFound_ThrowsResourceNotFound() {
        when(urlRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> urlService.deleteUrl(99L, samplePrincipal))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(urlRepository, never()).delete(any(Url.class));
    }

    @Test
    @DisplayName("toggleUrlStatus: chuyển đổi trạng thái isActive thành công và evict cache khỏi Redis")
    void toggleUrlStatus_Success() {
        Url url = Url.builder().id(10L).shortCode("code10").originalUrl("https://link10.com").user(sampleUser).isActive(true).build();
        when(urlRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(url));
        when(urlRepository.save(any(Url.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UrlResponse response = urlService.toggleUrlStatus(10L, samplePrincipal);

        assertThat(response).isNotNull();
        assertThat(response.getIsActive()).isFalse();
        verify(redisTemplate, times(1)).delete("url:code:code10");
    }
}
