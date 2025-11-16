package online.vkay.prepaidportal.common.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.vkay.prepaidportal.common.enums.ResponseStatus;
import online.vkay.prepaidportal.common.wrapper.DecryptedRequestWrapper;
import online.vkay.prepaidportal.common.wrapper.EncryptingResponseWrapper;
import online.vkay.prepaidportal.dto.BaseApiResponse;
import online.vkay.prepaidportal.service.CryptoKeyService;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;

/**
 * PayloadEncryptionFilter
 * ----------------------------------------------------
 * Transparently decrypts inbound payloads and encrypts outbound responses.
 * The cryptographic logic resides in request/response wrappers.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PayloadEncryptionFilter implements Filter {

    private final CryptoKeyService cryptoKeyService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    // ✅ Skip encryption/decryption for these endpoints
    private static final String[] PUBLIC_ENDPOINTS = {
            "/v1/public/**",
            "/v1/dev/**",
            "/actuator/**",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    };

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String uri = normalizeUri(httpRequest);

        log.debug("🔓 Decrypting inbound payload for {}", uri);

        try {
            // 🔓 Skip encryption/decryption for whitelisted endpoints
            if (isWhitelisted(uri)) {
                log.trace("Skipping payload encryption/decryption for public endpoint: {}", uri);
                chain.doFilter(request, response);
                return;
            }

            // Wrap request with decryption logic
            DecryptedRequestWrapper decryptedRequest = new DecryptedRequestWrapper(httpRequest, cryptoKeyService);

            // Wrap response with encryption logic (reuses AES key from request)
            EncryptingResponseWrapper encryptedResponse = new EncryptingResponseWrapper(httpResponse);

            // Proceed with decrypted data
            chain.doFilter(decryptedRequest, encryptedResponse);

            // Encrypt the outgoing response before sending
            encryptedResponse.encryptAndWrite();

        } catch (Exception e) {
            log.error("❌ Encryption/Decryption failed for {}: {}", uri, e.getMessage(), e);
            httpResponse.setStatus(ResponseStatus.GEN901.getHttpStatus().value());
            httpResponse.setContentType("application/json");

            BaseApiResponse<Object> errorResponse = BaseApiResponse.failure(ResponseStatus.GEN901, "Decryption/Encryption failed");

            httpResponse.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        }
    }

    private boolean isWhitelisted(String uri) {
        for (String pattern : PUBLIC_ENDPOINTS) {
            if (pathMatcher.match(pattern, uri)) return true;
        }
        return false;
    }

    private String normalizeUri(HttpServletRequest request) {
        String contextPath = request.getContextPath(); // e.g. "/api"
        String uri = request.getRequestURI();
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }
        return uri;
    }
}
