package com.school.platform.shared.domain.exception.compat;

import java.util.List;

public class ValidationException extends com.school.platform.shared.domain.exception.shared.ValidationException {
    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, List<String> errors) {
        super(message, errors);
    }
}