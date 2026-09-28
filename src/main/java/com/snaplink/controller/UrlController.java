package com.snaplink.controller;

import com.snaplink.config.ratelimit.RateLimit;
import com.snaplink.config.security.UserPrincipal;
import com.snaplink.dto.request.CreateUrlRequest;
import com.snaplink.dto.response.PageResponse;
import com.snaplink.dto.response.UrlResponse;
import com.snaplink.service.UrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/urls")
@RequiredArgsConstructor
@Tag(name = "URLs", description = "Endpoints tạo, quản lý danh sách, xem chi tiết và kích hoạt/xoá Short URLs")
public class UrlController {

    private final UrlService urlService;

    @PostMapping
    @RateLimit(guestLimit = 10, authLimit = 30, windowSeconds = 60, keyPrefix = "create_url")
    @Operation(
            summary = "Tạo link rút gọn (Short URL)",
            description = "Hỗ trợ cả người dùng Guest và Authenticated. Áp dụng Rate Limit (10 req/phút cho Guest, 30 req/phút cho User). Cho phép đặt custom alias (chỉ user) và expiration date.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Tạo link rút gọn thành công"),
                    @ApiResponse(responseCode = "400", description = "Dữ liệu hoặc format URL không hợp lệ"),
                    @ApiResponse(responseCode = "409", description = "Custom alias đã tồn tại"),
                    @ApiResponse(responseCode = "429", description = "Vượt quá giới hạn rate limit")
            }
    )
    public ResponseEntity<UrlResponse> createShortUrl(
            @Valid @RequestBody CreateUrlRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        UrlResponse response = urlService.createShortUrl(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(
            summary = "Lấy danh sách link rút gọn của người dùng hiện tại",
            description = "Trả về danh sách phân trang (mặc định 10 phần tử/trang, sắp xếp theo ngày tạo mới nhất).",
            security = @SecurityRequirement(name = "BearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
                    @ApiResponse(responseCode = "401", description = "Chưa xác thực JWT token")
            }
    )
    public ResponseEntity<PageResponse<UrlResponse>> getMyUrls(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        PageResponse<UrlResponse> response = urlService.getMyUrls(currentUser, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Xem chi tiết một link rút gọn",
            description = "Lấy thông tin chi tiết của link theo ID (yêu cầu quyền sở hữu).",
            security = @SecurityRequirement(name = "BearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lấy chi tiết thành công"),
                    @ApiResponse(responseCode = "401", description = "Chưa xác thực JWT token"),
                    @ApiResponse(responseCode = "404", description = "Không tìm thấy URL")
            }
    )
    public ResponseEntity<UrlResponse> getUrlById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        UrlResponse response = urlService.getUrlById(id, currentUser);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Xoá một link rút gọn",
            description = "Xoá link và tự động vô hiệu hoá cache Redis (Dual-eviction L1/L2).",
            security = @SecurityRequirement(name = "BearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "Xoá link thành công"),
                    @ApiResponse(responseCode = "401", description = "Chưa xác thực JWT token"),
                    @ApiResponse(responseCode = "404", description = "Không tìm thấy URL hoặc không thuộc quyền sở hữu")
            }
    )
    public ResponseEntity<Void> deleteUrl(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        urlService.deleteUrl(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @Operation(
            summary = "Bật/Tắt trạng thái hoạt động của link",
            description = "Chuyển đổi trạng thái isActive giữa true và false kèm đồng bộ xoá cache.",
            security = @SecurityRequirement(name = "BearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cập nhật trạng thái thành công"),
                    @ApiResponse(responseCode = "401", description = "Chưa xác thực JWT token"),
                    @ApiResponse(responseCode = "404", description = "Không tìm thấy URL")
            }
    )
    public ResponseEntity<UrlResponse> toggleUrlStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        UrlResponse response = urlService.toggleUrlStatus(id, currentUser);
        return ResponseEntity.ok(response);
    }
}
