package com.school.platform.shared.web;

import java.time.Instant;
import java.util.List;

public record SharedErrorResponse(
        int status,
        String message,
        String path,
        List<String> errors,
        Instant timestamp
) {
    public static SharedErrorResponse of(int status, String message, String path, List<String> errors) {
        return new SharedErrorResponse(status, message, path, errors == null ? List.of() : List.copyOf(errors), Instant.now());
    }
}
