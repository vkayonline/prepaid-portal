package online.vkay.prepaidportal.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import online.vkay.prepaidportal.entity.CryptoKey;
import online.vkay.prepaidportal.repository.CryptoKeyRepository;
import online.vkay.prepaidportal.utils.CryptoUtils;
import online.vkay.prepaidportal.utils.DateUtils;
import online.vkay.prepaidportal.utils.IdUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CryptoKeyService {

    private final CryptoKeyRepository repo;
    private static final Duration EXPIRY_BUFFER = Duration.ofMinutes(5);

    // ensure at least one active key on startup
    @PostConstruct
    public void init() {
        rotateKeyIfNeeded();
    }

    @Scheduled(cron = "0 0 0 1 * ?")
    public void scheduledKeyRotation() {
        rotateKeyIfNeeded();
    }

    // ----------------------------------------------------
    // Find key by KID but enforce expiry rules
    // ----------------------------------------------------
    public CryptoKey getByKid(String kid) {
        return repo.findByKid(kid, cutoff())
                .orElseThrow(() -> new IllegalStateException("Expired or invalid key for kid: " + kid));
    }

    // ----------------------------------------------------
    // Active key for ENCRYPT
    // ----------------------------------------------------
    public CryptoKey getActiveKey() {
        return repo.findActiveKey(cutoff())
                .orElseGet(this::rotateKeyIfNeeded);
    }

    // ----------------------------------------------------
    // Generate new key pair
    // ----------------------------------------------------
    @SneakyThrows
    public CryptoKey rotateKeyIfNeeded() {

        Optional<CryptoKey> active = repo.findActiveKey(cutoff());
        if (active.isPresent()) return active.get();

        // generate RSA key pair
        KeyPair keyPair = CryptoUtils.generateRsaKeyPair();
        var expiresAt = DateUtils.todayPlus(Period.ofMonths(1))
                .withDayOfMonth(1)
                .atStartOfDay();

        String publicPem = CryptoUtils.toPublicPem(keyPair.getPublic());
        String privatePem = CryptoUtils.toPrivatePem(keyPair.getPrivate());

        CryptoKey entity = new CryptoKey();
        entity.setKid(IdUtils.compactUuid());
        entity.setPublicKeyPem(publicPem);
        entity.setPrivateKeyPem(privatePem);
        entity.setExpiresAt(expiresAt);

        return repo.save(entity);
    }

    private LocalDateTime cutoff() {
        return DateUtils.nowMinus(EXPIRY_BUFFER);
    }
}


