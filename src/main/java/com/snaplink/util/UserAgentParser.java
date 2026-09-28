package com.snaplink.util;

import org.springframework.stereotype.Component;
import ua_parser.Client;
import ua_parser.Parser;

@Component
public class UserAgentParser {

    private final Parser parser;

    public UserAgentParser() {
        this.parser = new Parser();
    }

    public record UserAgentDetails(String deviceType, String browser, String os) {}

    public UserAgentDetails parse(String userAgentString) {
        if (userAgentString == null || userAgentString.isBlank()) {
            return new UserAgentDetails("UNKNOWN", "UNKNOWN", "UNKNOWN");
        }

        try {
            Client client = parser.parse(userAgentString);
            String uaLower = userAgentString.toLowerCase();

            // 1. Detect Device Type
            String deviceType;
            if (uaLower.contains("bot") || uaLower.contains("crawl") || uaLower.contains("spider") || uaLower.contains("curl")) {
                deviceType = "BOT";
            } else if (uaLower.contains("tablet") || uaLower.contains("ipad")) {
                deviceType = "TABLET";
            } else if (uaLower.contains("mobile") || uaLower.contains("android") || uaLower.contains("iphone")) {
                deviceType = "MOBILE";
            } else {
                deviceType = "DESKTOP";
            }

            // 2. Detect Browser
            String browser = "UNKNOWN";
            if (client.userAgent != null && client.userAgent.family != null && !client.userAgent.family.equals("Other")) {
                browser = client.userAgent.family;
            } else if (uaLower.contains("edg")) {
                browser = "Edge";
            } else if (uaLower.contains("chrome")) {
                browser = "Chrome";
            } else if (uaLower.contains("safari")) {
                browser = "Safari";
            } else if (uaLower.contains("firefox")) {
                browser = "Firefox";
            }

            // 3. Detect OS
            String os = "UNKNOWN";
            if (client.os != null && client.os.family != null && !client.os.family.equals("Other")) {
                os = client.os.family;
            } else if (uaLower.contains("windows")) {
                os = "Windows";
            } else if (uaLower.contains("mac")) {
                os = "macOS";
            } else if (uaLower.contains("android")) {
                os = "Android";
            } else if (uaLower.contains("ios") || uaLower.contains("iphone") || uaLower.contains("ipad")) {
                os = "iOS";
            } else if (uaLower.contains("linux")) {
                os = "Linux";
            }

            return new UserAgentDetails(deviceType, browser, os);
        } catch (Exception ex) {
            return new UserAgentDetails("UNKNOWN", "UNKNOWN", "UNKNOWN");
        }
    }
}
