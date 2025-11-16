package online.vkay.prepaidportal.common.wrapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import online.vkay.prepaidportal.common.context.CryptoSessionContext;
import online.vkay.prepaidportal.utils.CryptoUtils;

import javax.crypto.SecretKey;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Base64;

/**
 * Wrapper that captures the plaintext response,
 * encrypts it using the AES key stored in CryptoSessionContext,
 * and writes back the encrypted JSON envelope.
 */
@Slf4j
public class EncryptingResponseWrapper extends HttpServletResponseWrapper {

    private final CharArrayWriter buffer = new CharArrayWriter();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public EncryptingResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public PrintWriter getWriter() {
        return new PrintWriter(buffer);
    }

    public void encryptAndWrite() throws IOException {
        String plainResponse = buffer.toString();
        if (plainResponse == null || plainResponse.isBlank()) return;

        try {
            SecretKey aesKey = CryptoSessionContext.get();
            if (aesKey == null) {
                log.warn("No AES key in context — skipping encryption.");
                getResponse().getWriter().write(plainResponse);
                return;
            }

            byte[] iv = CryptoUtils.generateIv();
            byte[] plaintext = plainResponse.getBytes();
            byte[] ct = CryptoUtils.aesGcmEncrypt(aesKey, iv, plaintext, null)
                    .cipherTextWithTag();

            EncryptedResponse encryptedResponse = new EncryptedResponse(
                    Base64.getEncoder().encodeToString(iv),
                    Base64.getEncoder().encodeToString(ct)
            );

            getResponse().setContentType("application/json");
            getResponse().getWriter().write(MAPPER.writeValueAsString(encryptedResponse));
            getResponse().getWriter().flush();

            log.debug("✅ Response encrypted successfully");

        } catch (Exception e) {
            log.error("Failed to encrypt response", e);
            throw new IOException("Encryption failed", e);
        } finally {
            CryptoSessionContext.clear();
        }
    }

    // DTO for encrypted response
    private record EncryptedResponse(String iv, String ct) {
    }
}
