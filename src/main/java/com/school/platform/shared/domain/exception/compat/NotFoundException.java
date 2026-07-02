package com.school.platform.shared.domain.exception.compat;

public class NotFoundException extends com.school.platform.shared.domain.exception.shared.NotFoundException {
    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(resourceName, fieldName, fieldValue);
    }
}