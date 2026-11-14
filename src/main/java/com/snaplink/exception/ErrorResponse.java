package com.snaplink.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/** JSON lỗi dùng chung cho frontend: HTTP status, message, timestamp và lỗi theo field nếu có. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private int status;
    private String message;
    private Instant timestamp;
    private Map<String, String> errors;
}
