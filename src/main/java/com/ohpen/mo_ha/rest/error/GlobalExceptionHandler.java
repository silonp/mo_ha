package com.ohpen.mo_ha.rest.error;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.ohpen.mo_ha.service.exceptions.ConfigChangeNotFoundException;

import io.micrometer.tracing.Tracer;

//
// Let's do a global exception handling, even if we just have one RestController.
//
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Tracer tracer;
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Autowired
    public GlobalExceptionHandler(Tracer tracer) {
        this.tracer = tracer;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ConfigChangeErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe -> fieldErrors.put(fe.getField(), fe.getDefaultMessage()));
        return build(HttpStatus.BAD_REQUEST, "Validation failed", fieldErrors);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ConfigChangeErrorResponse> handleMalformed(Exception ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ConfigChangeErrorResponse> handleDomainInvariant(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ConfigChangeNotFoundException.class)
    public ResponseEntity<ConfigChangeErrorResponse> handleNotFound(ConfigChangeNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ConfigChangeErrorResponse> handleNoResourceFound(NoResourceFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ConfigChangeErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unhandled exception:", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
    }

    private ResponseEntity<ConfigChangeErrorResponse> build(HttpStatus status, String message) {
        return build(status, message, null);
    }

    private ResponseEntity<ConfigChangeErrorResponse> build(HttpStatus status, String message, Map<String, String> fieldErrors) {
        ConfigChangeErrorResponse body = new ConfigChangeErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                currentTraceId(),
                fieldErrors
        );
        return ResponseEntity.status(status).body(body);
    }

    private String currentTraceId() {
        try {
            return Objects.requireNonNull(tracer.currentSpan()).context().traceId();
        } catch (RuntimeException e) {
            log.error("Failed to get TraceID");
            return null;
        }
    }
}