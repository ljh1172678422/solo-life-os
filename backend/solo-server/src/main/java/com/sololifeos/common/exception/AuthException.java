package com.sololifeos.common.exception;

import com.sololifeos.common.response.ResultCode;

/**
 * Authentication or authorization failure (e.g. missing/invalid token,
 * insufficient role). Mapped to HTTP 401 by the global exception handler.
 */
public class AuthException extends SoloException {

    public AuthException(String message) {
        super(ResultCode.AUTH_ERROR.getCode(), message);
    }

    public AuthException(int code, String message) {
        super(code, message);
    }

    public AuthException(int code, String message, Throwable cause) {
        super(code, message, cause);
    }

}
