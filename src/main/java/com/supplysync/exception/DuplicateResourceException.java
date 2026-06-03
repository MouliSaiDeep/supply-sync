package com.supplysync.exception;

public class DuplicateResourceException extends RuntimeException {
    private String errorCode;

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
