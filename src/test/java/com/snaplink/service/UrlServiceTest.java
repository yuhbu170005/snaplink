package com.snaplink.service;

import com.snaplink.config.security.UserPrincipal;
import com.snaplink.dto.request.CreateUrlRequest;
import com.snaplink.dto.response.UrlResponse;
import com.snaplink.entity.Url;
import com.snaplink.entity.User;
import com.snaplink.exception.BadRequestException;
import com.snaplink.exception.ConflictException;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UrlService urlService;

    private User sampleUser;
    private UserPrincipal samplePrincipal;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(urlService, "baseUrl", "http://localhost:8080");

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
    @DisplayName("getMyUrls: trả về danh sách phân trang đúng cho user")
    void getMyUrls_Success() {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        Url url1 = Url.builder().id(1L).shortCode("code1").originalUrl("https://link1.com").user(sampleUser).isActive(true).createdAt(Instant.now()).build();
        org.springframework.data.domain.Page<Url> page = new org.springframework.data.domain.PageImpl<>(java.util.List.of(url1), pageable, 1);

        when(urlRepository.findByUserId(1L, pageable)).thenReturn(page);

        com.snaplink.dto.response.PageResponse<UrlResponse> response = urlService.getMyUrls(samplePrincipal, pageable);

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
                .isInstanceOf(com.snaplink.exception.ResourceNotFoundException.class)
                .hasMessageContaining("URL not found with id: 99");
    }

    @Test
    @DisplayName("deleteUrl: xoá thành công khi link thuộc về user")
    void deleteUrl_Success() {
        Url url = Url.builder().id(10L).shortCode("code10").originalUrl("https://link10.com").user(sampleUser).build();
        when(urlRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(url));

        urlService.deleteUrl(10L, samplePrincipal);

        verify(urlRepository, times(1)).delete(url);
    }

    @Test
    @DisplayName("deleteUrl: ném ResourceNotFoundException khi link không tồn tại hoặc không phải của user")
    void deleteUrl_NotFound_ThrowsResourceNotFound() {
        when(urlRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> urlService.deleteUrl(99L, samplePrincipal))
                .isInstanceOf(com.snaplink.exception.ResourceNotFoundException.class);

        verify(urlRepository, never()).delete(any(Url.class));
    }

    @Test
    @DisplayName("toggleUrlStatus: chuyển đổi trạng thái isActive thành công")
    void toggleUrlStatus_Success() {
        Url url = Url.builder().id(10L).shortCode("code10").originalUrl("https://link10.com").user(sampleUser).isActive(true).build();
        when(urlRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(url));
        when(urlRepository.save(any(Url.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UrlResponse response = urlService.toggleUrlStatus(10L, samplePrincipal);

        assertThat(response).isNotNull();
        assertThat(response.getIsActive()).isFalse();
    }
}
