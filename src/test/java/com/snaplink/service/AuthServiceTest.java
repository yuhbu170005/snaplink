package com.snaplink.service;

import com.snaplink.config.security.JwtTokenProvider;
import com.snaplink.config.security.UserPrincipal;
import com.snaplink.dto.request.LoginRequest;
import com.snaplink.dto.request.RegisterRequest;
import com.snaplink.dto.response.AuthResponse;
import com.snaplink.dto.response.UserResponse;
import com.snaplink.entity.User;
import com.snaplink.exception.BadRequestException;
import com.snaplink.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .email("test@snaplink.com")
                .passwordHash("hashed_password_123")
                .createdAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("register: thành công khi email chưa tồn tại")
    void register_Success() {
        RegisterRequest request = RegisterRequest.builder()
                .email("newuser@snaplink.com")
                .password("Password123!")
                .build();

        when(userRepository.existsByEmail("newuser@snaplink.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(tokenProvider.generateToken(sampleUser.getId(), sampleUser.getEmail())).thenReturn("mocked_jwt_token");
        when(tokenProvider.getExpirationInMs()).thenReturn(86400000L);

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("mocked_jwt_token");
        assertThat(response.getEmail()).isEqualTo("test@snaplink.com");
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("register: ném BadRequestException khi email đã tồn tại")
    void register_EmailExists_ThrowsBadRequestException() {
        RegisterRequest request = RegisterRequest.builder()
                .email("test@snaplink.com")
                .password("Password123!")
                .build();

        when(userRepository.existsByEmail("test@snaplink.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Email is already registered");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("login: thành công với thông tin đăng nhập đúng")
    void login_Success() {
        LoginRequest request = LoginRequest.builder()
                .email("test@snaplink.com")
                .password("Password123!")
                .build();

        when(userRepository.findByEmail("test@snaplink.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("Password123!", "hashed_password_123")).thenReturn(true);
        when(tokenProvider.generateToken(1L, "test@snaplink.com")).thenReturn("mocked_jwt_token");
        when(tokenProvider.getExpirationInMs()).thenReturn(86400000L);

        AuthResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("mocked_jwt_token");
        assertThat(response.getUserId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("login: ném BadRequestException khi mật khẩu sai")
    void login_WrongPassword_ThrowsBadRequestException() {
        LoginRequest request = LoginRequest.builder()
                .email("test@snaplink.com")
                .password("WrongPassword")
                .build();

        when(userRepository.findByEmail("test@snaplink.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("WrongPassword", "hashed_password_123")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    @DisplayName("login: ném BadRequestException khi không tìm thấy email")
    void login_UserNotFound_ThrowsBadRequestException() {
        LoginRequest request = LoginRequest.builder()
                .email("notfound@snaplink.com")
                .password("Password123!")
                .build();

        when(userRepository.findByEmail("notfound@snaplink.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    @DisplayName("getCurrentUser: trả về thông tin user đúng")
    void getCurrentUser_Success() {
        UserPrincipal principal = UserPrincipal.create(sampleUser);
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        UserResponse response = authService.getCurrentUser(principal);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@snaplink.com");
    }
}
