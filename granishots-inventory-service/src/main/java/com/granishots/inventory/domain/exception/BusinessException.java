package com.granishots.inventory.domain.exception;

public class BusinessException extends RuntimeException {
    private final int httpStatus;
    public BusinessException(String message, int httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }
    public int getHttpStatus() { return httpStatus; }
}
