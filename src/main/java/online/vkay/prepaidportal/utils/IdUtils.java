package online.vkay.prepaidportal.utils;

import lombok.experimental.UtilityClass;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Base64;
import java.util.UUID;

@UtilityClass
public class IdUtils {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    // Base62 alphabet
    private static final char[] ALPHA_NUMERIC_CHARS =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

    // ============================================================
    // 1. Normal UUID (collision-proof)
    // ============================================================

    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    // ============================================================
    // 2. Compact UUID (Base64 URL Safe, 22 chars)
    // ============================================================

    public static String compactUuid() {
        UUID uuid = UUID.randomUUID();

        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(buffer.array());
    }

    // ============================================================
    // 3. Prefix Added Time-Sortable Base62 ID
    // Format:
    //    <prefix>-<base62timestamp><base62random>
    // Example:
    //    TXN3n3lm8xEMw5Zk
    // ============================================================

    public static String compactId(String prefix) {
        String ts62 = base62Timestamp();     // sortable timestamp
        String rand = randomBase62(6);       // strong randomness
        return prefix + ts62 + rand;
    }

    // ============================================================
    // 4. Base62 Timestamp (sortable, very short)
    // Example: "3n3lm8xE"
    // ============================================================

    public static String base62Timestamp() {
        long epochMillisIST = Instant.now()
                .atZone(IST)
                .toInstant()
                .toEpochMilli();

        return toBase62(epochMillisIST);
    }

    // ============================================================
    // 5. Convert long → Base62 string
    // ============================================================

    public static String toBase62(long value) {
        if (value == 0) return "0";

        StringBuilder sb = new StringBuilder();
        while (value > 0) {
            int digit = (int) (value % 62);
            sb.append(ALPHA_NUMERIC_CHARS[digit]);
            value /= 62;
        }
        return sb.reverse().toString();   // maintain correct order
    }

    // ============================================================
    // 6. Random Base62 generator
    // ============================================================

    public static String randomBase62(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHA_NUMERIC_CHARS[RANDOM.nextInt(ALPHA_NUMERIC_CHARS.length)]);
        }
        return sb.toString();
    }
}
