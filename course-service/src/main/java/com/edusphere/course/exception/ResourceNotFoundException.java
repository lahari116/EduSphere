package com.edusphere.course.exception;
 
public class ResourceNotFoundException extends RuntimeException {
 
    // Constructor with message
    public ResourceNotFoundException(String message) {
        super(message);
    }
}