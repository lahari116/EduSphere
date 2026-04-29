package com.edusphere.assignment.exception;
 
public class ResourceNotFoundException extends RuntimeException {
 
    public ResourceNotFoundException(String message) {
        super(message);
    }
}