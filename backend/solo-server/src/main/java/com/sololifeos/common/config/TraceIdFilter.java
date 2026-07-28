package com.sololifeos.common.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Propagates a {@code traceId} across the full request lifecycle
 * (ARCHITECTURE §16 Observability).
 * <p>
 * For every inbound request the filter:
 * <ol>
 *   <li>reads an incoming {@code X-Trace-Id} header, or generates a new UUID
 *       when absent (e.g. frontend-issued trace id);</li>
 *   <li>stores it in the SLF4J MDC under the {@code traceId} key so both
 *       {@link com.sololifeos.common.response.ApiResponse} and the console
 *       logging pattern can pick it up;</li>
 *   <li>echoes it back on the response via the {@code X-Trace-Id} header;</li>
 *   <li>clears the MDC entry once the request completes to avoid leakage
 *       between pooled threads.</li>
 * </ol>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String MDC_TRACE_ID = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }
        MDC.put(MDC_TRACE_ID, traceId);
        response.setHeader(TRACE_ID_HEADER, traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_TRACE_ID);
        }
    }

}
