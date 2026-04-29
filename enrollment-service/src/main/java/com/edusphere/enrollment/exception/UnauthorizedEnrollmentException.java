package com.edusphere.enrollment.exception;

public class UnauthorizedEnrollmentException extends RuntimeException {
    public UnauthorizedEnrollmentException(String message) {
        super(message);
    }
}