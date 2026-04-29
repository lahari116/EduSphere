package com.edusphere.analytics_reporting.exception;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            ResourceNotFoundException ex) {

        Map<String, Object> res = new HashMap<>();
        res.put("timestamp", LocalDateTime.now());
        res.put("status", 404);
        res.put("message", ex.getMessage());

        return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
    }
}