package com.snaplink.exception;

/** Lỗi đầu vào hoặc quy tắc nghiệp vụ; GlobalExceptionHandler chuyển thành HTTP 400. */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
