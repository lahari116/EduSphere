package com.edusphere.enrollment.exception;

public class DepartmentMismatchException extends RuntimeException {
    public DepartmentMismatchException(String message) {
        super(message);
    }
}