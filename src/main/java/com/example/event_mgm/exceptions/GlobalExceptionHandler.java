package com.example.event_mgm.exceptions;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    String resourceNotFoundException(ResourceNotFoundException ex)
    {
        return ex.getLocalizedMessage();
    }
}
