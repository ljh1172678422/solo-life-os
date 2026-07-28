package com.sololifeos.common.exception;

import com.sololifeos.common.response.ApiResponse;
import com.sololifeos.common.response.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler that converts every thrown exception into the
 * unified {@link ApiResponse} format (ARCHITECTURE §20 / CODE_RULES §6 / §9.1).
 * <p>
 * Rules:
 * <ul>
 *   <li>Business-layer exceptions ({@link BusinessException},
 *       {@link ValidationException}, {@link AuthException}) -&gt; HTTP 4xx.</li>
 *   <li>System-layer exceptions ({@link AIException},
 *       {@link ExternalException}, {@link SoloException} base, generic
 *       {@link Exception}) -&gt; HTTP 500.</li>
 *   <li>Stack traces are never returned to the client; they are only logged
 *       server-side.</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Parameter validation failure -&gt; HTTP 400. */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(ValidationException ex) {
        log.warn("Validation error: code={}, message={}", ex.getCode(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getCode(), ex.getMessage()));
    }

    /** Business rule violation -&gt; HTTP 400. */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex) {
        log.warn("Business error: code={}, message={}", ex.getCode(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getCode(), ex.getMessage()));
    }

    /** Authentication / authorization failure -&gt; HTTP 401. */
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuth(AuthException ex) {
        log.warn("Auth error: code={}, message={}", ex.getCode(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ex.getCode(), ex.getMessage()));
    }

    /** AI invocation failure -&gt; HTTP 500. */
    @ExceptionHandler(AIException.class)
    public ResponseEntity<ApiResponse<Void>> handleAi(AIException ex) {
        log.error("AI error: code={}, message={}", ex.getCode(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ex.getCode(), ex.getMessage()));
    }

    /** External dependency failure -&gt; HTTP 500. */
    @ExceptionHandler(ExternalException.class)
    public ResponseEntity<ApiResponse<Void>> handleExternal(ExternalException ex) {
        log.error("External error: code={}, message={}", ex.getCode(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ex.getCode(), ex.getMessage()));
    }

    /** Catch-all for the Solo exception hierarchy -&gt; HTTP 500. */
    @ExceptionHandler(SoloException.class)
    public ResponseEntity<ApiResponse<Void>> handleSolo(SoloException ex) {
        log.error("System error: code={}, message={}", ex.getCode(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ex.getCode(), ex.getMessage()));
    }

    /** Bean Validation failure on @Valid request body -&gt; HTTP 400. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        StringBuilder builder = new StringBuilder("validation failed: ");
        boolean first = true;
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            if (!first) {
                builder.append("; ");
            }
            builder.append(fieldError.getField()).append(" ").append(fieldError.getDefaultMessage());
            first = false;
        }
        String message = builder.toString();
        log.warn("Method argument not valid: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ResultCode.VALIDATION_ERROR.getCode(), message));
    }

    /** Unexpected fallback -&gt; HTTP 500. Never expose the raw cause to the client. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex) {
        log.error("Unexpected system error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ResultCode.SYSTEM_ERROR.getCode(), ResultCode.SYSTEM_ERROR.getMessage()));
    }

}
