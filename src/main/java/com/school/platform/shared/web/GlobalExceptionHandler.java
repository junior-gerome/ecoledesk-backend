package com.school.platform.shared.web;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import com.school.platform.shared.domain.exception.BusinessException;
import com.school.platform.shared.domain.exception.ResourceNotFoundException;
import com.school.platform.shared.domain.exception.ValidationException;

import com.school.platform.shared.web.ApiResponse;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        logger.info("Business error: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(com.school.platform.shared.domain.exception.shared.BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleSharedBusinessException(
            com.school.platform.shared.domain.exception.shared.BusinessException ex) {
        logger.info("Business error: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    String fieldName = error instanceof FieldError fieldError ? fieldError.getField() : error.getObjectName();
                    return fieldName + ": " + error.getDefaultMessage();
                })
                .collect(Collectors.toList());
        return build(HttpStatus.BAD_REQUEST, "Erreur de validation", errors);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(BadCredentialsException ex) {
        logger.warn("Authentication failed: {}", ex.getMessage());
        return build(HttpStatus.UNAUTHORIZED, "Identifiants incorrects");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        logger.warn("Access denied: {}", ex.getMessage());
        return build(HttpStatus.FORBIDDEN, "Acces refuse");
    }

    @ExceptionHandler({ExpiredJwtException.class, SignatureException.class})
    public ResponseEntity<ApiResponse<Void>> handleJwtException(Exception ex) {
        logger.warn("JWT error: {}", ex.getMessage());
        return build(HttpStatus.UNAUTHORIZED, "Token JWT invalide ou expire");
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        logger.info("Resource not found: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(com.school.platform.shared.domain.exception.shared.NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleSharedNotFoundException(
            com.school.platform.shared.domain.exception.shared.NotFoundException ex) {
        logger.info("Resource not found: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(ValidationException ex) {
        logger.info("Validation failed: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), ex.getErrors());
    }

    @ExceptionHandler(com.school.platform.shared.domain.exception.shared.ValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleSharedValidationException(
            com.school.platform.shared.domain.exception.shared.ValidationException ex) {
        logger.info("Validation failed: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), ex.getErrors());
    }

    @ExceptionHandler(com.school.platform.shared.domain.exception.shared.UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorizedException(
            com.school.platform.shared.domain.exception.shared.UnauthorizedException ex) {
        logger.warn("Unauthorized: {}", ex.getMessage());
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(com.school.platform.shared.domain.exception.shared.ForbiddenException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbiddenException(
            com.school.platform.shared.domain.exception.shared.ForbiddenException ex) {
        logger.warn("Forbidden: {}", ex.getMessage());
        return build(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
        logger.warn("Authentication exception: {}", ex.getMessage());
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        logger.warn("Data integrity violation", ex);
        return build(HttpStatus.CONFLICT, "Contrainte d'integrite violee");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(ConstraintViolationException ex) {
        List<String> errors = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());
        return build(HttpStatus.BAD_REQUEST, "Erreur de validation", errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadableBody(HttpMessageNotReadableException ex) {
        logger.info("Unreadable request body: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "Corps de requete invalide");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParameter(MissingServletRequestParameterException ex) {
        return build(HttpStatus.BAD_REQUEST, "Parametre requis manquant: " + ex.getParameterName());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return build(HttpStatus.BAD_REQUEST, "Parametre invalide: " + ex.getName());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleResponseStatusException(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        HttpStatus resolvedStatus = status == null ? HttpStatus.INTERNAL_SERVER_ERROR : status;
        String message = ex.getReason() != null ? ex.getReason() : resolvedStatus.getReasonPhrase();
        return build(resolvedStatus, message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAll(Exception ex) {
        logger.error("Unexpected server error", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur serveur inattendue");
    }

    private ResponseEntity<ApiResponse<Void>> build(HttpStatus status, String message) {
        return build(status, message, List.of());
    }

    private ResponseEntity<ApiResponse<Void>> build(HttpStatus status, String message, List<String> errors) {
        return ResponseEntity.status(status).body(ApiResponse.error(message, errors));
    }
}
