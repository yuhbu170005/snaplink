package com.snaplink.util;

import java.util.Arrays;

/** Đổi số sang chuỗi gồm 0–9, a–z, A–Z. Đây là encoding, không phải mã hoá bảo mật. */
public final class Base62 {

    private static final String CHARSET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = 62;

    // Lookup table kích thước 128 (đủ bao quát bảng mã ASCII chuẩn)
    private static final byte[] DECODE_MAP = new byte[128];

    static {
        // Khởi tạo toàn bộ mảng với giá trị -1 đại diện cho ký tự không hợp lệ
        Arrays.fill(DECODE_MAP, (byte) -1);
        for (int i = 0; i < CHARSET.length(); i++) {
            DECODE_MAP[CHARSET.charAt(i)] = (byte) i;
        }
    }

    // 62^4 = 14,776,336  -> Đạt độ dài tối thiểu 5 ký tự
    // 62^5 = 916,132,832 -> Đạt độ dài tối thiểu 6 ký tự
    public static final long BASE_OFFSET = 14_776_336L; 

    private Base62() {
        // Utility class
    }

    /** Chia liên tiếp cho 62, lấy phần dư rồi đảo chuỗi để được mã Base62. */
    public static String encode(long number) {
        if (number < 0) {
            throw new IllegalArgumentException("Number must not be negative: " + number);
        }
        if (number == 0) {
            return String.valueOf(CHARSET.charAt(0));
        }

        StringBuilder encoded = new StringBuilder();
        long current = number;
        while (current > 0) {
            int remainder = (int) (current % BASE);
            encoded.append(CHARSET.charAt(remainder));
            current /= BASE;
        }

        return encoded.reverse().toString();
    }

    /** Nhân tích luỹ với 62; kiểm tra ký tự và tràn số trước mỗi bước. */
    public static long decode(String code) {
        if (code == null || code.isEmpty()) {
            throw new IllegalArgumentException("Input string must not be null or empty");
        }

        long result = 0;
        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);

            // Kiểm tra xem ký tự có nằm trong bảng ASCII chuẩn (0 - 127) không
            if (c >= DECODE_MAP.length || DECODE_MAP[c] == -1) {
                throw new IllegalArgumentException("Invalid Base62 character: " + c);
            }

            int value = DECODE_MAP[c];

            // Phòng ngừa tràn số kiểu long
            if (result > (Long.MAX_VALUE - value) / BASE) {
                throw new ArithmeticException("Base62 string overflows long type");
            }

            result = result * BASE + value;
        }

        return result;
    }
}