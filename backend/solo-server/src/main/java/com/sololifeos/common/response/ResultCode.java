package com.sololifeos.common.response;

/**
 * Common result codes shared across all modules (ARCHITECTURE §20 Error Handling).
 * <p>
 * Convention: {@code code = 0} means success. Business-layer errors use the
 * {@code 1xxx} range and map to HTTP 400; system-layer errors use the
 * {@code 5xxx} range and map to HTTP 500.
 * <p>
 * Module-specific error codes follow the {@code <MODULE>-ERR-<NNN>} prefix
 * convention and are defined by each module in later sprints, for example:
 * <ul>
 *   <li>{@code USER-ERR-001} - user not found</li>
 *   <li>{@code TODAY-ERR-001} - daily plan generation failed</li>
 *   <li>{@code AI-ERR-001} - llm invocation timeout</li>
 *   <li>{@code MOOD-ERR-*}, {@code GROWTH-ERR-*}, {@code EXPLORE-ERR-*},
 *       {@code COMMUNITY-ERR-*}, {@code STORY-ERR-*}</li>
 * </ul>
 * Only the common/generic codes are defined here.
 */
public enum ResultCode {

    /** Request succeeded. */
    SUCCESS(0, "success"),

    /** Generic business rule violation (HTTP 400). */
    BUSINESS_ERROR(1001, "business error"),

    /** Request parameter validation failure (HTTP 400). */
    VALIDATION_ERROR(1002, "validation error"),

    /** Authentication or authorization failure (HTTP 401/403). */
    AUTH_ERROR(1003, "authentication or authorization error"),

    /** Unexpected system failure (HTTP 500). */
    SYSTEM_ERROR(5000, "system error"),

    /** AI invocation failure (HTTP 500). */
    AI_ERROR(5001, "ai invocation error"),

    /** External dependency failure (HTTP 500). */
    EXTERNAL_ERROR(5002, "external dependency error");

    private final int code;

    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
