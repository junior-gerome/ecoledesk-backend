package com.school.gestionuser.infrastructure.exception;

import java.time.Instant;

public class ErrorResponse {
    private String message;
    private Instant timestamp = Instant.now();

    public ErrorResponse() {}

    public ErrorResponse(String message) {
        this.message = message;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Instant getTimestamp() { return timestamp; }
}