package com.edusphere.assignment.exception;

public class DeadlineExceededException extends RuntimeException {
    public DeadlineExceededException(String message) {
        super(message);
    }
}