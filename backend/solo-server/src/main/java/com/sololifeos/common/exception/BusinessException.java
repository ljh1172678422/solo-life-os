package com.sololifeos.common.exception;

import com.sololifeos.common.response.ResultCode;

/**
 * Business rule violation. Mapped to HTTP 400 by the global exception handler.
 */
public class BusinessException extends SoloException {

    public BusinessException(String message) {
        super(ResultCode.BUSINESS_ERROR.getCode(), message);
    }

    public BusinessException(int code, String message) {
        super(code, message);
    }

    public BusinessException(int code, String message, Throwable cause) {
        super(code, message, cause);
    }

}
