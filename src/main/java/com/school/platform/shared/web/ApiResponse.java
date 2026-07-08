package com.school.platform.shared.web;

import java.time.Instant;
import java.util.List;

public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        List<String> errors,
        Instant timestamp
) {
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data, List.of(), Instant.now());
    }

    public static <T> ApiResponse<T> error(String message) {
        return error(message, List.of());
    }

    public static <T> ApiResponse<T> error(String message, List<String> errors) {
        return new ApiResponse<>(false, message, null, errors == null ? List.of() : List.copyOf(errors), Instant.now());
    }
}
