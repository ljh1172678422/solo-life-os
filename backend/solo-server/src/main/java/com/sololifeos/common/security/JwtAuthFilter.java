package com.sololifeos.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sololifeos.common.response.ApiResponse;
import com.sololifeos.common.response.ResultCode;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * JWT 认证过滤器 (ADR-0006 JWT Authentication)。
 * <p>
 * 执行顺序：晚于 {@link com.sololifeos.common.config.TraceIdFilter}（后者 Order=HIGHEST_PRECEDENCE），
 * 以便 traceId 已注入 MDC 时再输出认证日志。
 * <p>
 * 流程：
 * <ol>
 *   <li>白名单路径直接放行（登录 / 注册 / 健康 / 文档）</li>
 *   <li>读取 {@code Authorization: Bearer <token>} 头</li>
 *   <li>调用 {@link JwtService#parseAndVerify} 验证</li>
 *   <li>验证成功：userId 写入 {@link UserContext}，放行</li>
 *   <li>验证失败：返回 401 + ApiResponse JSON</li>
 * </ol>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    /** Authorization 头前缀。 */
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * 白名单路径（Ant 模式匹配），无需 token 即可访问。
     * <p>
     * - /api/auth/login  登录端点
     * - /api/users POST  注册端点（MVP 开放注册）
     * - /health /actuator/**  健康检查与监控
     * - /swagger-ui/** /v3/api-docs/**  OpenAPI 文档
     * - /  根路径
     */
    private static final List<String> WHITELIST = List.of(
            "/",
            "/health",
            "/health/**",
            "/actuator/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/api/auth/login"
    );

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthFilter(JwtService jwtService, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // 白名单放行；POST /api/users 是注册端点（开放），其他 /api/** 需 token
        if (isWhitelisted(path, method)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 仅对 /api/** 路径强制 token，非 api 路径放行
        if (!path.startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            rejectUnauthorized(response, "缺少认证 token");
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();
        Optional<Claims> claims = jwtService.parseAndVerify(token);
        if (claims.isEmpty()) {
            rejectUnauthorized(response, "认证 token 无效或已过期");
            return;
        }

        Long userId = claims.get().get(JwtService.CLAIM_USER_ID, Long.class);
        if (userId == null) {
            rejectUnauthorized(response, "认证 token 缺少用户信息");
            return;
        }

        UserContext.setUserId(userId);
        log.debug("JWT 认证通过: userId={}, path={}", userId, path);
        try {
            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }

    /**
     * 判断请求是否在白名单内。
     * 注册端点 POST /api/users 开放（无 token），其他方法需 token。
     */
    private boolean isWhitelisted(String path, String method) {
        for (String pattern : WHITELIST) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        // POST /api/users 是注册端点，MVP 开放
        return "POST".equalsIgnoreCase(method) && "/api/users".equals(path);
    }

    /**
     * 返回 401 + 统一 ApiResponse JSON。不走 GlobalExceptionHandler（Filter 层异常不进 Controller）。
     */
    private void rejectUnauthorized(HttpServletResponse response, String message) throws IOException {
        log.warn("JWT 认证拒绝: {}", message);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        ApiResponse<Void> body = ApiResponse.error(ResultCode.AUTH_ERROR.getCode(), message);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
