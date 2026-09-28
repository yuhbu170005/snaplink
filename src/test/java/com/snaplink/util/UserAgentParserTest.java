package com.snaplink.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserAgentParserTest {

    private UserAgentParser userAgentParser;

    @BeforeEach
    void setUp() {
        userAgentParser = new UserAgentParser();
    }

    @Test
    @DisplayName("parse: Bóc tách Desktop Chrome trên macOS chính xác")
    void parse_MacChrome() {
        String ua = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
        UserAgentParser.UserAgentDetails details = userAgentParser.parse(ua);

        assertEquals("DESKTOP", details.deviceType());
        assertEquals("Chrome", details.browser());
        assertEquals("Mac OS X", details.os());
    }

    @Test
    @DisplayName("parse: Bóc tách Mobile iPhone Safari chính xác")
    void parse_IPhoneSafari() {
        String ua = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1";
        UserAgentParser.UserAgentDetails details = userAgentParser.parse(ua);

        assertEquals("MOBILE", details.deviceType());
        assertEquals("Mobile Safari", details.browser());
        assertEquals("iOS", details.os());
    }

    @Test
    @DisplayName("parse: Bóc tách Crawler / Bot chính xác")
    void parse_Bot() {
        String ua = "Googlebot/2.1 (+http://www.google.com/bot.html)";
        UserAgentParser.UserAgentDetails details = userAgentParser.parse(ua);

        assertEquals("BOT", details.deviceType());
    }

    @Test
    @DisplayName("parse: Xử lý chuỗi rỗng / null an toàn không crash")
    void parse_NullOrEmpty() {
        UserAgentParser.UserAgentDetails details1 = userAgentParser.parse(null);
        assertEquals("UNKNOWN", details1.deviceType());
        assertEquals("UNKNOWN", details1.browser());

        UserAgentParser.UserAgentDetails details2 = userAgentParser.parse("   ");
        assertEquals("UNKNOWN", details2.deviceType());
    }
}
