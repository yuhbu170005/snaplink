package com.snaplink.exception;

/** Tài nguyên không tồn tại hoặc không thuộc user; trả HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
