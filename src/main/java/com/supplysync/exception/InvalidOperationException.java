package com.supplysync.exception;

public class InvalidOperationException extends RuntimeException {
    private String errorCode;

    public InvalidOperationException(String message) {
        super(message);
    }

    public InvalidOperationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
