package com.snaplink.exception;

/** Dữ liệu trùng với bản ghi đã có; GlobalExceptionHandler chuyển thành HTTP 409. */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
