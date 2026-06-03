package com.supplysync.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private String getTimestamp() {
        return ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log.error("Resource not found exception: {}", ex.getMessage());
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(getTimestamp())
                .status(HttpStatus.NOT_FOUND.value())
                .errorCode("RESOURCE_NOT_FOUND")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .errors(new ArrayList<>())
                .build();
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(DuplicateResourceException ex, HttpServletRequest request) {
        log.error("Duplicate resource or conflict exception: {}", ex.getMessage());
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(getTimestamp())
                .status(HttpStatus.CONFLICT.value())
                .errorCode(ex.getErrorCode() != null ? ex.getErrorCode() : "RESOURCE_CONFLICT")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .errors(new ArrayList<>())
                .build();
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InsufficientInventoryException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientInventory(InsufficientInventoryException ex, HttpServletRequest request) {
        log.error("Insufficient inventory exception: {}", ex.getMessage());
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(getTimestamp())
                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .errorCode(ex.getErrorCode() != null ? ex.getErrorCode() : "INSUFFICIENT_INVENTORY")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .errors(ex.getErrors())
                .build();
        return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOperation(InvalidOperationException ex, HttpServletRequest request) {
        log.error("Invalid operation exception: {}", ex.getMessage());
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(getTimestamp())
                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .errorCode(ex.getErrorCode() != null ? ex.getErrorCode() : "INVALID_OPERATION")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .errors(new ArrayList<>())
                .build();
        return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.error("Access denied exception: {}", ex.getMessage());
        String errorCode = ex.getMessage().equals("SELF_APPROVAL_NOT_ALLOWED") ? "SELF_APPROVAL_NOT_ALLOWED" : "ACCESS_DENIED";
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(getTimestamp())
                .status(HttpStatus.FORBIDDEN.value())
                .errorCode(errorCode)
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .errors(new ArrayList<>())
                .build();
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ErrorResponse> handleTooManyRequests(TooManyRequestsException ex, HttpServletRequest request) {
        log.error("Too many requests exception: {}", ex.getMessage());
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(getTimestamp())
                .status(HttpStatus.TOO_MANY_REQUESTS.value())
                .errorCode("TOO_MANY_LOGIN_ATTEMPTS")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .errors(new ArrayList<>())
                .build();
        return new ResponseEntity<>(response, HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        log.error("Bad credentials exception: {}", ex.getMessage());
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(getTimestamp())
                .status(HttpStatus.UNAUTHORIZED.value())
                .errorCode("UNAUTHORIZED")
                .message("Invalid username or password")
                .path(request.getRequestURI())
                .errors(new ArrayList<>())
                .build();
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.error("Validation failed exception: {}", ex.getMessage());
        List<String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(getTimestamp())
                .status(HttpStatus.BAD_REQUEST.value())
                .errorCode("VALIDATION_FAILED")
                .message("Input validation failed")
                .path(request.getRequestURI())
                .errors(fieldErrors)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled generic exception: ", ex);
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(getTimestamp())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .errorCode("INTERNAL_SERVER_ERROR")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .errors(new ArrayList<>())
                .build();
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
