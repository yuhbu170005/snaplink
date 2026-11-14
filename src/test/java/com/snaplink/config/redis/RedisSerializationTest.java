package com.snaplink.config.redis;

import com.snaplink.dto.internal.UrlRedirectDto;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/** Round-trip serializer thật, không cần server Redis hay credential cloud. */
class RedisSerializationTest {
    @Test
    @SuppressWarnings("unchecked")
    void redirectDtoAndInstantSurviveJsonSerialization() {
        var template = new RedisConfig().redisTemplate(mock(RedisConnectionFactory.class));
        RedisSerializer<Object> serializer = (RedisSerializer<Object>) template.getValueSerializer();
        UrlRedirectDto original = UrlRedirectDto.builder().id(42L).shortCode("roundtrip")
                .originalUrl("https://example.com").isActive(true)
                .expiresAt(Instant.parse("2027-01-01T00:00:00Z")).build();
        Object decoded = serializer.deserialize(serializer.serialize(original));
        assertThat(decoded).isInstanceOf(UrlRedirectDto.class);
        assertThat(decoded).usingRecursiveComparison().isEqualTo(original);
    }
}
