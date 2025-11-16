package online.vkay.prepaidportal.common.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.vkay.prepaidportal.common.context.AuditContext;
import online.vkay.prepaidportal.entity.HttpAuditLog;
import online.vkay.prepaidportal.repository.HttpAuditLogRepository;
import online.vkay.prepaidportal.utils.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Enumeration;
import java.util.UUID;

/**
 * Handles inbound request auditing and logging (one audit record per request).
 * Generates requestId, populates MDC, logs headers, and persists a single audit_http record
 * containing both request and response metadata.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RequestLoggingFilter implements Filter {

    private static final Logger auditLog = LoggerFactory.getLogger("online.vkay.prepaidportal.audit");

    private final HttpAuditLogRepository auditRepository;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String requestId = IdUtils.compactUuid();
        String ip = req.getRemoteAddr();
        String uri = req.getRequestURI();
        String method = req.getMethod();
        String username = req.getHeader("X-User-Name");
        String sourceChannel = req.getHeader("X-Source-Channel");

        // ---- Setup MDC ----
        MDC.put("requestId", requestId);
        MDC.put("ip", ip);
        MDC.put("uri", uri);
        MDC.put("user", username != null ? username : "ANONYMOUS");

        // ---- Log inbound headers ----
        auditLog.info("INBOUND | uri={} | method={} | user={} | ip={}", uri, method, username, ip);
        log.info("Incoming [{} {}] headers={}", method, uri, extractHeaders(req));

        long startTime = System.currentTimeMillis();

        try {
            chain.doFilter(request, response);
        } finally {
            // ---- After controller returns ----
            long duration = System.currentTimeMillis() - startTime;
            int status = res.getStatus();

            String rc = null;
            String message = null;

            if (AuditContext.get() != null) {
                rc = AuditContext.get().getRc();
                message = AuditContext.get().getMessage();
            }

            // ---- Log completion ----
            auditLog.info("COMPLETED | uri={} | method={} | status={} | user={} | duration={}ms | rc={} | msg={}",
                    uri, method, status, username, duration, rc, message);

            // ---- Persist single audit record ----
            auditRepository.save(HttpAuditLog.builder()
                    .requestId(requestId)
                    .ip(ip)
                    .username(username)
                    .sourceChannel(sourceChannel)
                    .method(method)
                    .uri(uri)
                    .direction("INBOUND")
                    .status(status)
                    .durationMs(duration)
                    .rc(rc)
                    .message(message)
                    .build());

            AuditContext.clear();
            MDC.clear();
        }
    }

    private String extractHeaders(HttpServletRequest req) {
        StringBuilder sb = new StringBuilder();
        Enumeration<String> headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            if (!"authorization".equalsIgnoreCase(name)) {
                sb.append(name).append("=").append(req.getHeader(name)).append("; ");
            }
        }
        return sb.toString();
    }
}
