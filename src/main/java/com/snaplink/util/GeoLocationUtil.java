package com.snaplink.util;

import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

/** Tra cứu quốc gia offline bằng GeoLite2 Country; không gửi IP khách ra mạng. */
@Slf4j
@Component
public class GeoLocationUtil implements AutoCloseable {
    private final DatabaseReader reader;

    /** Spring truyền đường dẫn cấu hình; reader mở một lần và dùng chung giữa các worker. */
    public GeoLocationUtil(@Value("${app.geoip.database-path:}") String databasePath) {
        reader = openDatabase(databasePath);
    }

    private DatabaseReader openDatabase(String path) {
        if (path == null || path.isBlank()) {
            log.info("GeoIP database not configured; public IP country will be UNKNOWN");
            return null;
        }
        try {
            return new DatabaseReader.Builder(new File(path)).withCache(new CHMCache()).build();
        } catch (IOException ex) {
            // GeoIP là metadata phụ trợ. File thiếu/hỏng không được chặn app khởi động.
            log.warn("Cannot open GeoIP database; country lookup disabled: {}", ex.getMessage());
            return null;
        }
    }

    /** Trả mã ISO (VD: VN), LOCAL cho mạng nội bộ, UNKNOWN nếu không tra cứu được. */
    public String resolveCountry(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) return "UNKNOWN";
        String ip = ipAddress.split(",")[0].trim();
        InetAddress address = parseLiteralIp(ip);
        if (address == null) return "UNKNOWN";
        if (isPrivateAddress(address)) return "LOCAL";
        if (reader == null) return "UNKNOWN";
        try {
            return reader.tryCountry(address)
                    .map(response -> response.country().isoCode())
                    .filter(code -> !code.isBlank())
                    .orElse("UNKNOWN");
        } catch (IOException | GeoIp2Exception ex) {
            log.debug("GeoIP lookup failed: {}", ex.getMessage());
            return "UNKNOWN";
        }
    }

    public boolean isLocalOrPrivateIp(String ip) {
        InetAddress address = parseLiteralIp(ip);
        return address != null && isPrivateAddress(address);
    }

    /** Chỉ parse IP literal; không để InetAddress thực hiện DNS từ header do client nhập. */
    private InetAddress parseLiteralIp(String ip) {
        if (ip == null || ip.isBlank()) return null;
        if (ip.equalsIgnoreCase("localhost")) return InetAddress.getLoopbackAddress();
        boolean ipv4 = ip.matches("[0-9]{1,3}(\\.[0-9]{1,3}){3}");
        boolean ipv6 = ip.contains(":") && ip.matches("[0-9a-fA-F:.]+");
        if (!ipv4 && !ipv6) return null;
        try {
            return InetAddress.getByName(ip);
        } catch (IOException ex) {
            return null;
        }
    }

    private boolean isPrivateAddress(InetAddress address) {
        byte[] bytes = address.getAddress();
        boolean uniqueLocalIpv6 = bytes.length == 16 && (bytes[0] & 0xfe) == 0xfc;
        return address.isAnyLocalAddress() || address.isLoopbackAddress()
                || address.isSiteLocalAddress() || address.isLinkLocalAddress()
                || address.isMulticastAddress() || uniqueLocalIpv6;
    }

    /** Spring gọi khi shutdown để đóng file reader. */
    @PreDestroy
    public void close() throws IOException {
        if (reader != null) reader.close();
    }
}
