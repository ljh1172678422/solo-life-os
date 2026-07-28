package com.sololifeos.common.exception;

import com.sololifeos.common.response.ResultCode;

/**
 * AI invocation failure (e.g. LLM timeout, agent routing error). Mapped to
 * HTTP 500 by the global exception handler.
 */
public class AIException extends SoloException {

    public AIException(String message) {
        super(ResultCode.AI_ERROR.getCode(), message);
    }

    public AIException(int code, String message) {
        super(code, message);
    }

    public AIException(int code, String message, Throwable cause) {
        super(code, message, cause);
    }

}
