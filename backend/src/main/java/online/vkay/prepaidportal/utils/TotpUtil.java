package online.vkay.prepaidportal.utils;

import org.apache.commons.codec.binary.Base32;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.Instant;

public class TotpUtil {

    private static final int TIME_STEP_SECONDS = 30;
    private static final int TOTP_DIGITS = 6;
    private static final String HMAC_ALGO = "HmacSHA1";

    /**
     * Generate a Base32 secret for a new user
     */
    public static String generateSecret() {
        byte[] buffer = new byte[10]; // 80 bits
        new SecureRandom().nextBytes(buffer);
        return new Base32().encodeToString(buffer);
    }

    /**
     * Generate otpauth URI for Google Authenticator QR
     */
    public static String generateOtpAuthUri(String issuer, String username, String secret) {
        return String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s",
                issuer, username, secret, issuer
        );
    }

    /**
     * Verify user-entered TOTP with ±1 time window tolerance
     */
    public static boolean verifyCode(String secret, String userCode) {
        long currentWindow = Instant.now().getEpochSecond() / TIME_STEP_SECONDS;

        for (long i = -1; i <= 1; i++) { // ±30 seconds tolerance
            String expected = generateTotp(secret, currentWindow + i);
            if (expected.equals(userCode)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Core TOTP algorithm (RFC 6238)
     */
    private static String generateTotp(String secret, long timeWindow) {
        try {
            Base32 base32 = new Base32();
            byte[] key = base32.decode(secret);

            ByteBuffer buffer = ByteBuffer.allocate(8);
            buffer.putLong(timeWindow);
            byte[] timeBytes = buffer.array();

            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(key, HMAC_ALGO));
            byte[] hmac = mac.doFinal(timeBytes);

            int offset = hmac[hmac.length - 1] & 0x0F;
            int binary =
                    ((hmac[offset] & 0x7f) << 24)
                            | ((hmac[offset + 1] & 0xff) << 16)
                            | ((hmac[offset + 2] & 0xff) << 8)
                            | (hmac[offset + 3] & 0xff);

            int otp = binary % (int) Math.pow(10, TOTP_DIGITS);
            return String.format("%0" + TOTP_DIGITS + "d", otp);
        } catch (Exception e) {
            throw new RuntimeException("Error generating TOTP", e);
        }
    }
}
