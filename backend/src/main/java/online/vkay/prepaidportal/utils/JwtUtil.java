package online.vkay.prepaidportal.utils;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    private final byte[] secret;
    private final String issuer;
    private final Duration expirySeconds;

    public JwtUtil(online.vkay.prepaidportal.config.JwtProperties props) {
        this.secret = props.getSecret().getBytes();
        this.issuer = props.getIssuer();
        this.expirySeconds = Duration.ofSeconds(props.getExpirySeconds());
    }

    /**
     * Generate a JWT token with claims
     */
    public String generateToken(String subject, Map<String, Object> claims) {
        try {
            var now = Instant.now();
            JWSSigner signer = new MACSigner(secret);

            JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                    .subject(subject)
                    .issuer(issuer)
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plus(expirySeconds)));

            if (claims != null) {
                claims.forEach(claimsBuilder::claim);
            }

            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader(JWSAlgorithm.HS256),
                    claimsBuilder.build()
            );

            signedJWT.sign(signer);
            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Error generating JWT", e);
        }
    }

    /**
     * Validate a JWT token
     */
    public boolean validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(secret);

            boolean signatureValid = signedJWT.verify(verifier);
            if (!signatureValid) return false;

            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            Date now = new Date();

            long bufferMillis = 60 * 1000L;

            return expirationTime.getTime() + bufferMillis > now.getTime();
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * Extract the subject (user id, email, etc.)
     */
    public String getSubject(String token) {
        try {
            return SignedJWT.parse(token).getJWTClaimsSet().getSubject();
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Extract a specific claim
     */
    public Object getClaim(String token, String claimKey) {
        try {
            return SignedJWT.parse(token).getJWTClaimsSet().getClaim(claimKey);
        } catch (ParseException e) {
            return null;
        }
    }
}
