package online.vkay.prepaidportal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import online.vkay.prepaidportal.service.CryptoKeyService;
import online.vkay.prepaidportal.utils.CryptoUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/dev")
@RequiredArgsConstructor
public class DevController {

    private final CryptoKeyService keyService;
    private final SecretKey staticAesKey = CryptoUtils.generateAesKey();

    // ---------------------------------------------------------
    // Encrypt endpoint (simulate CLIENT → SERVER request)
    // ---------------------------------------------------------
    @PostMapping("/encrypt")
    public Map<String, Object> encryptDev(@RequestBody Map<String, Object> payload) throws Exception {

        // 1. Convert payload to JSON bytes
        String json = new ObjectMapper().writeValueAsString(payload);
        byte[] plaintext = json.getBytes(StandardCharsets.UTF_8);
        byte[] iv = CryptoUtils.generateIv();

        // 3. Encrypt payload via AES-GCM
        CryptoUtils.EncryptionResult aesResult =
                CryptoUtils.aesGcmEncrypt(staticAesKey, iv, plaintext, null);

        // 4. Wrap AES key using server active RSA public key
        var active = keyService.getActiveKey();
        PublicKey publicKey = CryptoUtils.loadPublicKeyFromPem(active.getPublicKeyPem());

        byte[] wrappedAesKey = CryptoUtils.rsaEncrypt(publicKey, staticAesKey.getEncoded());

        // 5. Build response (same structure client must send)
        Map<String, Object> out = new HashMap<>();
        out.put("kid", active.getKid());
        out.put("iv", Base64.getUrlEncoder().withoutPadding().encodeToString(iv));
        out.put("ct", Base64.getUrlEncoder().withoutPadding().encodeToString(aesResult.cipherTextWithTag()));
        out.put("cek", Base64.getUrlEncoder().withoutPadding().encodeToString(wrappedAesKey));

        return out;
    }

    // ---------------------------------------------------------
    // Decrypt endpoint (simulate SERVER → CLIENT decoding)
    // ---------------------------------------------------------
    @PostMapping("/decrypt")
    public Map<String, Object> decryptDev(@RequestBody Map<String, Object> body) throws Exception {

        String ivBase64 = (String) body.get("iv");
        String cipherBase64 = (String) body.get("ct");

        byte[] iv = Base64.getUrlDecoder().decode(ivBase64);
        byte[] cipher = Base64.getUrlDecoder().decode(cipherBase64);

        byte[] plaintext = CryptoUtils.aesGcmDecrypt(staticAesKey, iv, cipher, null);

        // 4. Convert back to JSON object
        String json = new String(plaintext, StandardCharsets.UTF_8);
        return new ObjectMapper().readValue(json, Map.class);
    }
}

