package com.snaplink.integration;

import com.snaplink.entity.ClickEvent;
import com.snaplink.repository.ClickEventRepository;
import com.snaplink.repository.UrlRepository;
import com.snaplink.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Chạy HTTP → Security → Service → H2 thật. Chỉ mock Redis, không dùng Redis cloud. */
@SpringBootTest(properties = {
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.datasource.url=jdbc:h2:mem:snaplinkflow;MODE=PostgreSQL;DB_CLOSE_DELAY=-1"
})
@AutoConfigureMockMvc
class BackendFlowTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UrlRepository urls;
    @Autowired UserRepository users;
    @Autowired ClickEventRepository clicks;
    @MockitoBean RedisTemplate<String, Object> redisTemplate;
    @MockitoBean StringRedisTemplate stringRedisTemplate;

    @Test
    void guestCannotReadProtectedEndpoints() throws Exception {
        for (String path : new String[]{"/api/auth/me", "/api/urls", "/api/urls/1/analytics"}) {
            mvc.perform(get(path)).andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401));
        }
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer broken"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void ownerCanCreateReadToggleDeleteAndOtherUsersCannotRead() throws Exception {
        String owner = register();
        String stranger = register();
        // Alias dài hơn 16 ký tự từng bị schema cũ từ chối.
        String alias = "newbie-friendly-link-" + UUID.randomUUID();
        JsonNode url = create(owner, alias);
        long id = url.get("id").asLong();
        mvc.perform(get("/api/urls").header("Authorization", owner))
                .andExpect(status().isOk()).andExpect(jsonPath("$.pageNumber").value(0));
        mvc.perform(get("/api/urls/" + id).header("Authorization", stranger)).andExpect(status().isNotFound());
        mvc.perform(get("/api/urls/" + id + "/analytics").header("Authorization", stranger)).andExpect(status().isNotFound());
        mvc.perform(get("/" + alias)).andExpect(status().isFound()).andExpect(header().string("Location", "https://example.com"));
        mvc.perform(patch("/api/urls/" + id + "/status").header("Authorization", owner))
                .andExpect(status().isOk()).andExpect(jsonPath("$.isActive").value(false));
        // Redirect trước đó đã nạp L1; disable phải vô hiệu hoá cả cache.
        mvc.perform(get("/" + alias)).andExpect(status().isNotFound());
        mvc.perform(delete("/api/urls/" + id).header("Authorization", owner)).andExpect(status().isNoContent());
        mvc.perform(get("/api/urls/" + id).header("Authorization", owner)).andExpect(status().isNotFound());
    }

    @Test
    void analyticsUsesStoredClickTimeAndIncludesDirectTraffic() throws Exception {
        String owner = register();
        long id = create(owner, "analytics-" + UUID.randomUUID()).get("id").asLong();
        // Dữ liệu đã lưu; không phụ thuộc thời gian worker xử lý hoặc delay bất đồng bộ.
        clicks.saveAndFlush(ClickEvent.builder().url(urls.findById(id).orElseThrow())
                .clickedAt(Instant.parse("2025-01-01T23:59:59Z")).ipAddress("1.1.1.1")
                .browser("Firefox").country("VN").deviceType("DESKTOP").build());
        mvc.perform(get("/api/urls/" + id + "/analytics").header("Authorization", owner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalClicks").value(1))
                .andExpect(jsonPath("$.uniqueVisitors").value(1))
                .andExpect(jsonPath("$.clicksOverTime[0].timestamp").value("2025-01-01"))
                .andExpect(jsonPath("$.referrers[0].name").value("Direct"))
                .andExpect(jsonPath("$.referrers[0].percentage").value(100.0));
    }

    @Test
    void guestLinksHaveNoPrivateAnalyticsAndInvalidJsonReturns400() throws Exception {
        JsonNode guest = json.readTree(mvc.perform(post("/api/urls").contentType("application/json")
                .content("{\"originalUrl\":\"https://example.com\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        mvc.perform(get("/api/urls/" + guest.get("id").asLong() + "/analytics")
                .header("Authorization", register())).andExpect(status().isNotFound());
        mvc.perform(post("/api/urls").contentType("application/json").content("{broken"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registrationLoginAndUniqueEmailWork() throws Exception {
        String email = UUID.randomUUID() + "@example.com";
        String body = "{\"email\":\"" + email + "\",\"password\":\"safe-password\"}";
        mvc.perform(post("/api/auth/register").contentType("application/json").content(body)).andExpect(status().isCreated());
        mvc.perform(post("/api/auth/register").contentType("application/json").content(body)).andExpect(status().isConflict());
        String token = json.readTree(mvc.perform(post("/api/auth/login").contentType("application/json").content(body))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("accessToken").asText();
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.email").value(email));
        assertThat(users.findByEmail(email).orElseThrow().getPasswordHash()).doesNotContain("safe-password");
    }

    @Test
    void openApiDocumentationIsAvailableWithoutAuthentication() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/urls']").exists());
    }

    private String register() throws Exception {
        String email = UUID.randomUUID() + "@example.com";
        String response = mvc.perform(post("/api/auth/register").contentType("application/json")
                .content("{\"email\":\"" + email + "\",\"password\":\"safe-password\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return "Bearer " + json.readTree(response).get("accessToken").asText();
    }

    private JsonNode create(String authorization, String alias) throws Exception {
        String response = mvc.perform(post("/api/urls").header("Authorization", authorization).contentType("application/json")
                .content("{\"originalUrl\":\"https://example.com\",\"customAlias\":\"" + alias + "\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return json.readTree(response);
    }
}
