package cl.tenpo.learning.reactive.tasks.task2.infrastructure.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public Mono<ResponseEntity<ErrorDetails>> handleRuntimeException(RuntimeException ex, ServerWebExchange exchange) {
        log.error("RuntimeException: {}", ex.getMessage());
        
        ErrorDetails errorDetails = ErrorDetails.builder()
                .timestamp(LocalDateTime.now())
                .message(ex.getMessage())
                .details("An unexpected error occurred")
                .path(exchange.getRequest().getPath().value())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .build();
                
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDetails));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ErrorDetails>> handleIllegalArgumentException(IllegalArgumentException ex, ServerWebExchange exchange) {
        log.error("IllegalArgumentException: {}", ex.getMessage());
        
        ErrorDetails errorDetails = ErrorDetails.builder()
                .timestamp(LocalDateTime.now())
                .message(ex.getMessage())
                .details("Invalid input parameters")
                .path(exchange.getRequest().getPath().value())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .build();
                
        return Mono.just(ResponseEntity.badRequest().body(errorDetails));
    }

    @ExceptionHandler(IllegalStateException.class)
    public Mono<ResponseEntity<ErrorDetails>> handleIllegalStateException(IllegalStateException ex, ServerWebExchange exchange) {
        log.error("IllegalStateException: {}", ex.getMessage());
        
        ErrorDetails errorDetails = ErrorDetails.builder()
                .timestamp(LocalDateTime.now())
                .message(ex.getMessage())
                .details("Invalid state for operation")
                .path(exchange.getRequest().getPath().value())
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .build();
                
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(errorDetails));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleValidationErrors(WebExchangeBindException ex, ServerWebExchange exchange) {
        log.error("Validation error: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Failed");
        response.put("path", exchange.getRequest().getPath().value());
        
        response.put("details", ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        error -> error.getField(),
                        error -> error.getDefaultMessage(),
                        (existing, replacement) -> existing + ", " + replacement
                )));
        
        return Mono.just(ResponseEntity.badRequest().body(response));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleConstraintViolation(ConstraintViolationException ex, ServerWebExchange exchange) {
        log.error("Constraint violation: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Failed");
        response.put("path", exchange.getRequest().getPath().value());
        
        response.put("details", ex.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        violation -> violation.getMessage(),
                        (existing, replacement) -> existing + ", " + replacement
                )));
        
        return Mono.just(ResponseEntity.badRequest().body(response));
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public Mono<ResponseEntity<ErrorDetails>> handleServiceUnavailableException(ServiceUnavailableException ex, ServerWebExchange exchange) {
        log.error("ServiceUnavailableException: {}", ex.getMessage());
        
        ErrorDetails errorDetails = ErrorDetails.builder()
                .timestamp(LocalDateTime.now())
                .message(ex.getMessage())
                .details("External service unavailable or timeout")
                .path(exchange.getRequest().getPath().value())
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .error(HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase())
                .build();
                
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorDetails));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public Mono<ResponseEntity<ErrorDetails>> handleUnauthorizedException(UnauthorizedException ex, ServerWebExchange exchange) {
        log.error("UnauthorizedException: {}", ex.getMessage());
        
        ErrorDetails errorDetails = ErrorDetails.builder()
                .timestamp(LocalDateTime.now())
                .message(ex.getMessage())
                .details("Authentication required")
                .path(exchange.getRequest().getPath().value())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .build();
                
        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorDetails));
    }
}
