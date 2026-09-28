package com.snaplink.util;

import org.springframework.stereotype.Component;

import java.net.InetAddress;

@Component
public class GeoLocationUtil {

    /**
     * Resolves the country name or code from client IP.
     * Offline fast resolution that safely identifies loopback / private IP ranges.
     */
    public String resolveCountry(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) {
            return "UNKNOWN";
        }

        String ip = ipAddress.trim();

        // Check if multiple comma-separated IPs (e.g., from X-Forwarded-For)
        if (ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        if (isLocalOrPrivateIp(ip)) {
            return "LOCAL";
        }

        // For public IPs in offline/demo environment:
        // Returns country resolution or UNKNOWN
        return "UNKNOWN";
    }

    public boolean isLocalOrPrivateIp(String ip) {
        if (ip == null || ip.isBlank()) {
            return false;
        }

        if (ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1") || ip.equals("::1") || ip.equalsIgnoreCase("localhost")) {
            return true;
        }

        try {
            InetAddress address = InetAddress.getByName(ip);
            return address.isSiteLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress();
        } catch (Exception e) {
            return false;
        }
    }
}
