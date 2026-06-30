package com.school.attendance.adapter.in.rest;

import com.school.attendance.application.service.AttendanceNotFoundException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AttendanceExceptionHandler {
    @ExceptionHandler(AttendanceNotFoundException.class)
    ResponseEntity<Map<String, Object>> notFound(AttendanceNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage(), List.of());
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class})
    ResponseEntity<Map<String, Object>> badRequest(Exception exception) {
        List<String> errors = exception instanceof MethodArgumentNotValidException validationException
                ? validationException.getBindingResult().getFieldErrors().stream()
                        .map(error -> error.getField() + ": " + error.getDefaultMessage())
                        .toList()
                : List.of();
        return error(HttpStatus.BAD_REQUEST, "Requete de presence invalide", errors);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, Object>> serverError(Exception exception) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur serveur inattendue", List.of());
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message, List<String> errors) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("status", status.value());
        body.put("message", message);
        body.put("errors", errors);
        body.put("timestamp", Instant.now().toString());
        return ResponseEntity.status(status).body(body);
    }
}
