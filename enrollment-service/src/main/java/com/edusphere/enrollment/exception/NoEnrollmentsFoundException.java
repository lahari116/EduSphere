package com.edusphere.enrollment.exception;

public class NoEnrollmentsFoundException extends RuntimeException {
    public NoEnrollmentsFoundException(String message) {
        super(message);
    }
}