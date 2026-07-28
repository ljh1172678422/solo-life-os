package com.sololifeos.common.response;

import org.slf4j.MDC;

/**
 * Unified API response wrapper (ARCHITECTURE §11 API Boundary).
 * <p>
 * Successful responses use {@code code = 0}. The {@code traceId} field is
 * automatically populated from the MDC so it can be correlated across the
 * full request lifecycle (frontend -&gt; backend -&gt; AI).
 *
 * <pre>
 * {
 *   "code": 0,
 *   "message": "success",
 *   "data": {},
 *   "traceId": "xxx"
 * }
 * </pre>
 */
public class ApiResponse<T> {

    /** Success code. Any non-zero value indicates an error. */
    private final int code;

    private final String message;

    private final T data;

    private final String traceId;

    public ApiResponse(int code, String message, T data, String traceId) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.traceId = traceId;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public String getTraceId() {
        return traceId;
    }

    /**
     * Build a success response without payload.
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null, currentTraceId());
    }

    /**
     * Build a success response carrying a payload.
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data, currentTraceId());
    }

    /**
     * Build a success response carrying a payload and a custom message.
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(ResultCode.SUCCESS.getCode(), message, data, currentTraceId());
    }

    /**
     * Build an error response with the given code and message.
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null, currentTraceId());
    }

    private static String currentTraceId() {
        return MDC.get("traceId");
    }

}
