package com.school.platform.shared.domain.exception.shared;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s '%s'", resourceName, fieldName, fieldValue));
    }
}
