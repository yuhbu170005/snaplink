package com.snaplink.config.async;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/** Tạo thread pool riêng cho tracking; tách tác vụ ghi click khỏi luồng trả redirect. */
@Configuration
@EnableAsync
public class AsyncConfig {

    public static final String CLICK_TRACKING_EXECUTOR = "clickTrackingExecutor";

    /** Tạo worker pool có hàng đợi hữu hạn để tránh dùng RAM không giới hạn. */
    @Bean(name = CLICK_TRACKING_EXECUTOR)
    public Executor clickTrackingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("click-tracker-");
        // Khi hàng đợi đầy, từ chối telemetry; tuyệt đối không chạy nó trên luồng redirect.
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.initialize();
        return executor;
    }
}
