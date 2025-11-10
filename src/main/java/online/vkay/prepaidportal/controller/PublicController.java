package online.vkay.prepaidportal.controller;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import online.vkay.prepaidportal.entity.CryptoKey;
import online.vkay.prepaidportal.service.CryptoKeyService;
import online.vkay.prepaidportal.utils.DateUtils;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/v1/public")
@RequiredArgsConstructor
public class PublicController {

    private final CryptoKeyService cryptoKeyService;

    @GetMapping("/keys")
    public ResponseEntity<PublicKeyResponse> getActivePublicKey() {

        CryptoKey key = cryptoKeyService.getActiveKey();

        var resp = PublicKeyResponse.builder()
                .kid(key.getKid())
                .publicKeyPem(key.getPublicKeyPem())
                .expiresAt(DateUtils.format(key.getExpiresAt()))
                .build();

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES))
                .body(resp);
    }

    @Builder
    public record PublicKeyResponse(String kid, String publicKeyPem, String expiresAt) {
    }

}
