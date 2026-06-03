package com.supplysync.exception;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class InsufficientInventoryException extends RuntimeException {
    private String errorCode;
    private final List<String> errors;

    public InsufficientInventoryException(String message) {
        super(message);
        this.errors = new ArrayList<>();
    }

    public InsufficientInventoryException(String message, List<String> errors) {
        super(message);
        this.errors = errors;
    }

    public InsufficientInventoryException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.errors = new ArrayList<>();
    }

    public InsufficientInventoryException(String errorCode, String message, List<String> errors) {
        super(message);
        this.errorCode = errorCode;
        this.errors = errors;
    }
}
