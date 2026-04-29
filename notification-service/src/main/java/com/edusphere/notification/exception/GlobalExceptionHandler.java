package com.edusphere.notification.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {

        Map<String, Object> res = new HashMap<>();
        res.put("timestamp", LocalDateTime.now());
        res.put("status", 404);
        res.put("error", "Not Found");
        res.put("message", ex.getMessage());

        return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UnauthorizedActionException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorized(UnauthorizedActionException ex) {

        Map<String, Object> res = new HashMap<>();
        res.put("timestamp", LocalDateTime.now());
        res.put("status", 403);
        res.put("error", "Forbidden");
        res.put("message", ex.getMessage());

        return new ResponseEntity<>(res, HttpStatus.FORBIDDEN);
    }
}