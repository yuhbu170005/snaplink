package com.snaplink.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Base62Test {

    @Test
    @DisplayName("encode & decode: chuyển đổi 2 chiều chính xác")
    void encodeAndDecode_Success() {
        long[] testNumbers = {0, 1, 61, 62, 12345, 10000000L, 9876543210L};

        for (long num : testNumbers) {
            String encoded = Base62.encode(num);
            long decoded = Base62.decode(encoded);

            assertThat(encoded).isNotBlank();
            assertThat(decoded).isEqualTo(num);
        }
    }

    @Test
    @DisplayName("encode với BASE_OFFSET tạo ra chuỗi độ dài tối thiểu")
    void encode_WithOffset_HasProperLength() {
        String encoded = Base62.encode(1L + Base62.BASE_OFFSET);
        assertThat(encoded.length()).isGreaterThanOrEqualTo(4);
    }
}
