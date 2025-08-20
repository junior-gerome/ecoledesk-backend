package com.school.exception;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValidationErrorResponse extends ErrorResponse {
    private String field;
    private String message;

    public ValidationErrorResponse(int status, String message, LocalDateTime timestamp, String field) {
        super(status, message, timestamp);
        this.field = field;
        this.message = message;
    }
}