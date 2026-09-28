package com.snaplink.controller;

import com.snaplink.config.security.UserPrincipal;
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

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints quản lý đăng ký, đăng nhập và thông tin tài khoản người dùng")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(
            summary = "Đăng ký tài khoản mới",
            description = "Tạo tài khoản mới với email và mật khẩu. Trả về JWT Access Token.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Đăng ký thành công"),
                    @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
                    @ApiResponse(responseCode = "409", description = "Email đã tồn tại")
            }
    )
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Đăng nhập hệ thống",
            description = "Xác thực email và mật khẩu, trả về JWT Access Token.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Đăng nhập thành công"),
                    @ApiResponse(responseCode = "400", description = "Sai email hoặc mật khẩu")
            }
    )
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
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserPrincipal currentUser) {
        UserResponse response = authService.getCurrentUser(currentUser);
        return ResponseEntity.ok(response);
    }
}
