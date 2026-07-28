package com.sololifeos.common.exception;

/**
 * Base exception for all Solo Life OS domain errors (ARCHITECTURE §20).
 * <p>
 * Carries a numeric {@code code} (see {@link com.sololifeos.common.response.ResultCode})
 * alongside the human readable message. Subclasses categorize the failure:
 * {@link BusinessException}, {@link ValidationException}, {@link AIException},
 * {@link ExternalException} and {@link AuthException}.
 */
public class SoloException extends RuntimeException {

    private final int code;

    public SoloException(int code, String message) {
        super(message);
        this.code = code;
    }

    public SoloException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

}
