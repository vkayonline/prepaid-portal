package online.vkay.prepaidportal.common.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.vkay.prepaidportal.common.context.UserContext;
import online.vkay.prepaidportal.common.enums.ResponseStatus;
import online.vkay.prepaidportal.dto.BaseApiResponse;
import online.vkay.prepaidportal.entity.User;
import online.vkay.prepaidportal.repository.UserRepository;
import online.vkay.prepaidportal.utils.JwtUtil;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.util.Optional;

/**
 * Handles JWT-based authorization without Spring Security.
 * Enforces validation, user existence, and blocks unauthorized access.
 * Supports Ant-style path patterns for public endpoints.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthorizationFilter implements Filter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    // ✅ Flexible public endpoints with wildcards
    private static final String[] PUBLIC_ENDPOINTS = {
            "/v1/auth/check-sso",
            "/v1/public/**",
            "/actuator/**",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    };

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String uri = normalizeUri(req);

        try {
            // 🔓 Skip public endpoints
            if (isPublic(uri)) {
                chain.doFilter(request, response);
                return;
            }

            String authHeader = req.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendError(res, ResponseStatus.GEN301, "Missing or invalid Authorization header");
                return;
            }

            String token = authHeader.substring(7);
            if (!jwtUtil.validateToken(token)) {
                sendError(res, ResponseStatus.GEN301, "Invalid or expired token");
                return;
            }

            String subject = jwtUtil.getSubject(token);
            Optional<User> userOpt = userRepository.findById(Long.parseLong(subject));

            if (userOpt.isEmpty()) {
                sendError(res, ResponseStatus.GEN301, "User not found");
                return;
            }

            User user = userOpt.get();

            // ✅ Attach context + MDC
            UserContext.setUser(user);
            MDC.put("user", user.getEmail());

            log.debug("✅ JWT validated for user={} uri={}", user.getEmail(), uri);
            chain.doFilter(request, response);

        } catch (Exception e) {
            log.error("Authorization filter error for {}: {}", uri, e.getMessage(), e);
            sendError((HttpServletResponse) response, ResponseStatus.GEN400, "Authorization failure");
        } finally {
            UserContext.clear();
        }
    }

    /**
     * Removes servlet context path (e.g., "/api") to ensure
     * pattern matching is performed on the relative URI.
     */
    private String normalizeUri(HttpServletRequest request) {
        String contextPath = request.getContextPath(); // e.g. "/api"
        String uri = request.getRequestURI();
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }
        return uri;
    }

    /**
     * Checks whether the given URI matches any public (whitelisted) pattern.
     */
    private boolean isPublic(String uri) {
        for (String pattern : PUBLIC_ENDPOINTS) {
            if (pathMatcher.match(pattern, uri)) {
                log.trace("Skipping auth filter for public endpoint: {}", uri);
                return true;
            }
        }
        return false;
    }

    private void sendError(HttpServletResponse res, ResponseStatus status, String customMessage) throws IOException {
        res.setStatus(status.getHttpStatus().value());
        res.setContentType("application/json");

        BaseApiResponse<Object> errorResponse = BaseApiResponse.failure(status, customMessage);
        res.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
