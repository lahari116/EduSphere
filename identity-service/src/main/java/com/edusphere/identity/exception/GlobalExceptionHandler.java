package com.edusphere.identity.exception;
 
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
 
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
 
@RestControllerAdvice
public class GlobalExceptionHandler {
 
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
 
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", 404);
        response.put("error", "Not Found");
        response.put("message", ex.getMessage());
 
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
 
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
 
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", 400);
        response.put("error", "Bad Request");
        response.put("message", ex.getMessage());
 
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

 // ✅ DTO validation errors (@Valid)
     @ExceptionHandler(MethodArgumentNotValidException.class)
     public ResponseEntity<Map<String, String>> handleValidationErrors(
             MethodArgumentNotValidException ex) {

         Map<String, String> errors = new HashMap<>();

         for (FieldError error : ex.getBindingResult().getFieldErrors()) {
             errors.put(error.getField(), error.getDefaultMessage());
         }

         return ResponseEntity.badRequest().body(errors);
     }

}