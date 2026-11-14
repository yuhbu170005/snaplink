package com.snaplink.service;

import com.snaplink.config.security.JwtTokenProvider;
import com.snaplink.config.security.UserPrincipal;
import com.snaplink.dto.request.LoginRequest;
import com.snaplink.dto.request.RegisterRequest;
import com.snaplink.dto.response.AuthResponse;
import com.snaplink.dto.response.UserResponse;
import com.snaplink.entity.User;
import com.snaplink.exception.BadRequestException;
import com.snaplink.exception.ResourceNotFoundException;
import com.snaplink.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Locale;
import java.nio.charset.StandardCharsets;
import org.springframework.dao.DataIntegrityViolationException;
import com.snaplink.exception.ConflictException;
import org.springframework.transaction.annotation.Transactional;

/** Nghiệp vụ tài khoản. Password chỉ lưu bản BCrypt hash; JWT được tạo khi xác thực thành công. */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    /** Tạo tài khoản và trả JWT. Database UNIQUE là chốt chặn khi nhiều request cùng đăng ký. */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        validatePasswordLength(request.getPassword());
        String normalizedEmail = request.getEmail().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException("Email is already registered");
        }

        User user = User.builder()
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        User savedUser;
        try {
            savedUser = userRepository.save(user);
            userRepository.flush(); // Bắt cả trường hợp hai request cùng đăng ký một email.
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Email is already registered");
        }

        return createAuthResponse(savedUser);
    }

    /** So sánh password với BCrypt hash; thông báo chung để không tiết lộ email có tồn tại. */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        validatePasswordLength(request.getPassword());
        String normalizedEmail = request.getEmail().trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Invalid email or password");
        }

        return createAuthResponse(user);
    }

    private void validatePasswordLength(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BadRequestException("Password must not exceed 72 UTF-8 bytes");
        }
    }

    /** Đăng ký và đăng nhập trả cùng một contract; expiresIn giữ đơn vị millisecond. */
    private AuthResponse createAuthResponse(User user) {
        return AuthResponse.builder()
                .accessToken(tokenProvider.generateToken(user.getId(), user.getEmail()))
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getExpirationInMs())
                .userId(user.getId())
                .email(user.getEmail())
                .build();
    }

    /** Đọc profile theo ID của token đã xác thực, không lấy ID do client tự gửi. */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(UserPrincipal currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUser.getId()));

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
