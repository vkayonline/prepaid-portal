package online.vkay.prepaidportal.common.wrapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;
import online.vkay.prepaidportal.common.context.CryptoSessionContext;
import online.vkay.prepaidportal.service.CryptoKeyService;
import online.vkay.prepaidportal.utils.CryptoUtils;

import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Wrapper that reads an encrypted request body and replaces it
 * with decrypted plaintext content using AES-GCM + RSA hybrid logic.
 */
@Slf4j
public class DecryptedRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] decryptedBody;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public DecryptedRequestWrapper(HttpServletRequest request, CryptoKeyService cryptoKeyService) throws IOException {
        super(request);

        String encryptedBody = readBody(request);
        if (encryptedBody == null || encryptedBody.isBlank()) {
            this.decryptedBody = new byte[0];
            return;
        }

        try {
            EncryptedRequest encReq = MAPPER.readValue(encryptedBody, EncryptedRequest.class);

            // Load RSA private key
            var keyEntity = cryptoKeyService.getByKid(encReq.kid());
            var privateKey = CryptoUtils.loadPrivateKeyFromPem(keyEntity.getPrivateKeyPem());

            // RSA unwrap AES key
            byte[] cekBytes = Base64.getDecoder().decode(encReq.cek());
            SecretKey aesKey = CryptoUtils.loadAesKey(CryptoUtils.rsaDecrypt(privateKey, cekBytes));

            // AES decrypt
            byte[] iv = Base64.getDecoder().decode(encReq.iv());
            byte[] ct = Base64.getDecoder().decode(encReq.ct());
            byte[] aad = encReq.aad() != null ? Base64.getDecoder().decode(encReq.aad()) : null;

            byte[] plain = CryptoUtils.aesGcmDecrypt(aesKey, iv, ct, aad);

            // Store AES key for response encryption
            CryptoSessionContext.set(aesKey);
            this.decryptedBody = plain;

            log.debug("✅ Request decrypted successfully for kid={}", encReq.kid());

        } catch (Exception e) {
            log.error("Decryption failed", e);
            throw new IOException("Decryption failed", e);
        }
    }

    private String readBody(HttpServletRequest req) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(req.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }

    @Override
    public ServletInputStream getInputStream() {
        ByteArrayInputStream bais = new ByteArrayInputStream(decryptedBody);
        return new ServletInputStream() {
            @Override
            public int read() {
                return bais.read();
            }

            @Override
            public boolean isFinished() {
                return bais.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
            }
        };
    }

    private record EncryptedRequest(String kid, String cek, String iv, String ct, String aad, String nonce, long ts) {
    }
}
