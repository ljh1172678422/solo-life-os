package com.sololifeos.common.exception;

import com.sololifeos.common.response.ResultCode;

/**
 * Request parameter validation failure. Mapped to HTTP 400 by the global
 * exception handler.
 */
public class ValidationException extends SoloException {

    public ValidationException(String message) {
        super(ResultCode.VALIDATION_ERROR.getCode(), message);
    }

    public ValidationException(int code, String message) {
        super(code, message);
    }

    public ValidationException(int code, String message, Throwable cause) {
        super(code, message, cause);
    }

}
