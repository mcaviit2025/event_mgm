package com.example.event_mgm.exceptions;

import com.example.event_mgm.dto.common.ErrorResponseDto;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidCredentials(
            InvalidCredentialsException ex, WebRequest request) {

        log.error("Invalid credentials: {}", ex.getMessage());

        ErrorResponseDto error = ErrorResponseDto.builder()
                .errorCode("AUTH_001")
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDto> handleBadCredentials(
            BadCredentialsException ex, WebRequest request) {

        log.error("Bad credentials: {}", ex.getMessage());

        ErrorResponseDto error = ErrorResponseDto.builder()
                .errorCode("AUTH_002")
                .message("Invalid username or password")
                .path(request.getDescription(false))
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorResponseDto> handleExpiredJwt(
            ExpiredJwtException ex, WebRequest request) {

        log.error("Expired JWT token: {}", ex.getMessage());

        ErrorResponseDto error = ErrorResponseDto.builder()
                .errorCode("AUTH_003")
                .message("Token has expired. Please login again.")
                .path(request.getDescription(false))
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorResponseDto> handleJwtException(
            JwtException ex, WebRequest request) {

        log.error("JWT exception: {}", ex.getMessage());

        ErrorResponseDto error = ErrorResponseDto.builder()
                .errorCode("AUTH_004")
                .message("Invalid token")
                .path(request.getDescription(false))
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDenied(
            AccessDeniedException ex, WebRequest request) {

        log.error("Access denied: {}", ex.getMessage());

        ErrorResponseDto error = ErrorResponseDto.builder()
                .errorCode("AUTH_005")
                .message("You don't have permission to access this resource")
                .path(request.getDescription(false))
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleResourceNotFound(
            ResourceNotFoundException ex, WebRequest request) {

        log.error("Resource not found: {}", ex.getMessage());

        ErrorResponseDto error = ErrorResponseDto.builder()
                .errorCode("RES_001")
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(InvalidData.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidData(
            InvalidData ex, WebRequest request) {

        log.error("Invalid data: {}", ex.getMessage());

        ErrorResponseDto error = ErrorResponseDto.builder()
                .errorCode("VAL_001")
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {

        log.error("Validation error: {}", ex.getMessage());

        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                validationErrors.put(error.getField(), error.getDefaultMessage())
        );

        ErrorResponseDto error = ErrorResponseDto.builder()
                .errorCode("VAL_002")
                .message("Validation failed")
                .validationErrors(validationErrors)
                .path(request.getDescription(false))
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {

        log.error("Constraint violation: {}", ex.getMessage());

        Map<String, String> validationErrors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation ->
                validationErrors.put(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()
                )
        );

        ErrorResponseDto error = ErrorResponseDto.builder()
                .errorCode("VAL_003")
                .message("Validation failed")
                .validationErrors(validationErrors)
                .path(request.getDescription(false))
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleAllExceptions(
            Exception ex, WebRequest request) {

        log.error("Unhandled exception: {}", ex.getMessage(), ex);

        ErrorResponseDto error = ErrorResponseDto.builder()
                .errorCode("SYS_001")
                .message("An unexpected error occurred. Please try again later.")
                .path(request.getDescription(false))
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler
    public String dulicate(DuplicateResourceException ex)
    {
        return ex.getMessage();
    }
}