package com.snaplink.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeoLocationUtilTest {

    private GeoLocationUtil geoLocationUtil;

    @BeforeEach
    void setUp() {
        geoLocationUtil = new GeoLocationUtil();
    }

    @Test
    @DisplayName("resolveCountry: Nhận diện chính xác Local/Loopback IP")
    void resolveCountry_LocalIp() {
        assertEquals("LOCAL", geoLocationUtil.resolveCountry("127.0.0.1"));
        assertEquals("LOCAL", geoLocationUtil.resolveCountry("::1"));
        assertEquals("LOCAL", geoLocationUtil.resolveCountry("192.168.1.100"));
        assertEquals("LOCAL", geoLocationUtil.resolveCountry("10.0.0.5"));
    }

    @Test
    @DisplayName("resolveCountry: Xử lý chuỗi nhiều IP (X-Forwarded-For)")
    void resolveCountry_MultipleIps() {
        assertEquals("LOCAL", geoLocationUtil.resolveCountry("127.0.0.1, 10.0.0.1"));
    }

    @Test
    @DisplayName("resolveCountry: Xử lý null hoặc chuỗi trống")
    void resolveCountry_NullOrBlank() {
        assertEquals("UNKNOWN", geoLocationUtil.resolveCountry(null));
        assertEquals("UNKNOWN", geoLocationUtil.resolveCountry("   "));
    }
}
