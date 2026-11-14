package com.snaplink.controller;

import com.snaplink.config.security.UserPrincipal;
import com.snaplink.config.ratelimit.RateLimit;
import com.snaplink.dto.request.LoginRequest;
import com.snaplink.dto.request.RegisterRequest;
import com.snaplink.dto.response.AuthResponse;
import com.snaplink.dto.response.UserResponse;
import com.snaplink.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** Nhận HTTP đăng ký/đăng nhập/profile, validate DTO rồi chuyển việc xử lý sang AuthService. */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints quản lý đăng ký, đăng nhập và thông tin tài khoản người dùng")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @RateLimit(guestLimit = 5, authLimit = 5, windowSeconds = 60, keyPrefix = "register")
    @Operation(
            summary = "Đăng ký tài khoản mới",
            description = "Tạo tài khoản mới với email và mật khẩu. Trả về JWT Access Token.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Đăng ký thành công"),
                    @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
                    @ApiResponse(responseCode = "409", description = "Email đã tồn tại")
            }
    )
    /** Tạo tài khoản và trả JWT. Database UNIQUE là chốt chặn khi nhiều request cùng đăng ký. */
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @RateLimit(guestLimit = 10, authLimit = 10, windowSeconds = 60, keyPrefix = "login")
    @Operation(
            summary = "Đăng nhập hệ thống",
            description = "Xác thực email và mật khẩu, trả về JWT Access Token.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Đăng nhập thành công"),
                    @ApiResponse(responseCode = "400", description = "Sai email hoặc mật khẩu")
            }
    )
    /** So sánh password với BCrypt hash; thông báo chung để không tiết lộ email có tồn tại. */
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(
            summary = "Lấy thông tin người dùng hiện tại",
            description = "Trả về thông tin chi tiết của người dùng đang đăng nhập dựa trên JWT token.",
            security = @SecurityRequirement(name = "BearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
                    @ApiResponse(responseCode = "401", description = "Chưa xác thực hoặc token không hợp lệ")
            }
    )
    /** Đọc profile theo ID của token đã xác thực, không lấy ID do client tự gửi. */
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserPrincipal currentUser) {
        UserResponse response = authService.getCurrentUser(currentUser);
        return ResponseEntity.ok(response);
    }
}
