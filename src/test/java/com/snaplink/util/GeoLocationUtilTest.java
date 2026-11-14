package com.snaplink.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeoLocationUtilTest {

    private GeoLocationUtil geoLocationUtil;

    @BeforeEach
    void setUp() {
        geoLocationUtil = new GeoLocationUtil("");
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
    @Test
    void noDatabaseOrInvalidIpReturnsUnknownWithoutDnsLookup() {
        assertEquals("UNKNOWN", geoLocationUtil.resolveCountry("8.8.8.8"));
        assertEquals("UNKNOWN", geoLocationUtil.resolveCountry("example.com"));
        assertEquals("UNKNOWN", geoLocationUtil.resolveCountry("999.999.999.999"));
        assertEquals("LOCAL", geoLocationUtil.resolveCountry("fd00::1"));
    }

    @Test
    void missingDatabaseDoesNotPreventApplicationFromRunning() throws Exception {
        GeoLocationUtil unavailable = new GeoLocationUtil("/not-present/GeoLite2-Country.mmdb");
        assertEquals("UNKNOWN", unavailable.resolveCountry("8.8.8.8"));
        unavailable.close();
    }
    @Test
    void readsActualMaxMindCountryDatabaseFixture() throws Exception {
        String path = java.nio.file.Path.of(getClass().getResource("/geoip/GeoIP2-Country-Test.mmdb").toURI()).toString();
        try (GeoLocationUtil configured = new GeoLocationUtil(path)) {
            assertEquals("GB", configured.resolveCountry("81.2.69.160"));
            assertEquals("JP", configured.resolveCountry("2001:218::"));
            assertEquals("UNKNOWN", configured.resolveCountry("1.1.1.1"));
            assertEquals("LOCAL", configured.resolveCountry("127.0.0.1"));
        }
    }

}
