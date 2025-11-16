package online.vkay.prepaidportal.repository;

import online.vkay.prepaidportal.entity.CryptoKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface CryptoKeyRepository extends JpaRepository<CryptoKey, Long> {

    @Query(
            value = """
                    SELECT * FROM crypto_keys
                    WHERE kid = :kid
                      AND expires_at > :cutoff
                    LIMIT 1
                    """,
            nativeQuery = true
    )
    Optional<CryptoKey> findByKid(@Param("kid") String kid,
                                  @Param("cutoff") LocalDateTime cutoff);

    @Query(
            value = """
                    SELECT * FROM crypto_keys
                    WHERE expires_at > :cutoff
                    ORDER BY created_at DESC
                    LIMIT 1
                    """,
            nativeQuery = true
    )
    Optional<CryptoKey> findActiveKey(@Param("cutoff") LocalDateTime cutoff);
}


