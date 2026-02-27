package com.itq.document_station.handler;


import com.itq.document_station.dto.MessageResponse;
import com.itq.document_station.exception.AuthenticationException;
import com.itq.document_station.exception.EntityNotFoundException;
import com.itq.document_station.exception.InvalidOperationException;
import com.itq.document_station.exception.TokenRefreshException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<MessageResponse> handleNotFound(EntityNotFoundException ex) {
        log.error("Entity not found: {}", ex.getMessage());
        MessageResponse apiError = new MessageResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }

    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<MessageResponse> handleInvalidOperation(InvalidOperationException ex) {
        log.error("Invalid operation: {}", ex.getMessage());
        MessageResponse apiError = new MessageResponse(HttpStatus.CONFLICT.value(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
    }

    @ExceptionHandler(TokenRefreshException.class)
    public ResponseEntity<MessageResponse> handleTokenRefreshOperation(TokenRefreshException ex) {
        log.error("Not authenticated: {}", ex.getMessage());
        MessageResponse apiError = new MessageResponse(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiError);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<MessageResponse> handleAuthentication(AuthenticationException ex) {
        log.error("Token not found: {}", ex.getMessage());
        MessageResponse apiError = new MessageResponse(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiError);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<MessageResponse> handleAuthentication(BadCredentialsException ex) {
        log.error("Неверные учетные данные пользователя: {}", ex.getMessage());
        MessageResponse apiError = new MessageResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<MessageResponse> handleValidationExceptions(DataIntegrityViolationException ex) {
        log.error("Validation error: {}", ex.getMessage());
        String message = "Validation failed";
        MessageResponse apiError = new MessageResponse(HttpStatus.BAD_REQUEST.value(), message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageResponse> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        MessageResponse apiError = new MessageResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }
}
