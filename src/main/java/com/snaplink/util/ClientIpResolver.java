package com.snaplink.util;

import jakarta.servlet.http.HttpServletRequest;

/** Dùng chung cách lấy IP cho rate limit và analytics để hai luồng không bị lệch nhau. */
public final class ClientIpResolver {
    private ClientIpResolver() {}

    /** Chỉ tin header khi máy chủ nằm sau proxy đã lọc/ghi đè header từ client. */
    public static String resolve(HttpServletRequest request, boolean trustProxyHeaders) {
        if (trustProxyHeaders) {
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                return forwarded.split(",")[0].trim();
            }
            String realIp = request.getHeader("X-Real-IP");
            if (realIp != null && !realIp.isBlank()) {
                return realIp.trim();
            }
        }
        String remoteAddress = request.getRemoteAddr();
        return remoteAddress == null || remoteAddress.isBlank() ? "unknown" : remoteAddress;
    }
}
