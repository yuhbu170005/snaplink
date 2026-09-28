package com.snaplink.controller;

import com.snaplink.config.security.UserPrincipal;
import com.snaplink.dto.response.analytics.AnalyticsResponse;
import com.snaplink.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/urls")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Endpoints xem thống kê chi tiết lượt truy cập của Short URLs")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/{id}/analytics")
    @Operation(
            summary = "Xem thống kê chi tiết lượt click của Short URL",
            description = "Trả về tổng click, unique visitors, biểu đồ click theo thời gian, tỷ lệ thiết bị, trình duyệt, quốc gia và top referrer.",
            security = @SecurityRequirement(name = "BearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lấy dữ liệu thống kê thành công"),
                    @ApiResponse(responseCode = "401", description = "Chưa xác thực JWT token"),
                    @ApiResponse(responseCode = "404", description = "Không tìm thấy URL hoặc không thuộc quyền sở hữu")
            }
    )
    public ResponseEntity<AnalyticsResponse> getAnalytics(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long currentUserId = userPrincipal != null ? userPrincipal.getId() : null;
        AnalyticsResponse response = analyticsService.getAnalytics(id, currentUserId);
        return ResponseEntity.ok(response);
    }
}
