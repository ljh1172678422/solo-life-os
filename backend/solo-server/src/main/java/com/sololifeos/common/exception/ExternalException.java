package com.sololifeos.common.exception;

import com.sololifeos.common.response.ResultCode;

/**
 * External dependency failure (e.g. weather / map / payment / push provider
 * unavailable). Mapped to HTTP 500 by the global exception handler.
 */
public class ExternalException extends SoloException {

    public ExternalException(String message) {
        super(ResultCode.EXTERNAL_ERROR.getCode(), message);
    }

    public ExternalException(int code, String message) {
        super(code, message);
    }

    public ExternalException(int code, String message, Throwable cause) {
        super(code, message, cause);
    }

}
