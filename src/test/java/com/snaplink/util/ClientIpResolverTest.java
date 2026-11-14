package com.snaplink.util;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.assertj.core.api.Assertions.assertThat;

class ClientIpResolverTest {
    @Test
    void spoofedHeaderIsIgnoredUnlessProxyTrustIsExplicitlyEnabled() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        request.addHeader("X-Forwarded-For", "1.2.3.4, 10.0.0.2");
        assertThat(ClientIpResolver.resolve(request, false)).isEqualTo("10.0.0.1");
        assertThat(ClientIpResolver.resolve(request, true)).isEqualTo("1.2.3.4");
    }
}
