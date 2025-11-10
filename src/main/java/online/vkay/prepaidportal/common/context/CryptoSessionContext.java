package online.vkay.prepaidportal.common.context;

import lombok.Builder;
import lombok.Data;

import javax.crypto.SecretKey;

@Data
@Builder
public class CryptoSessionContext {
    private static final ThreadLocal<SecretKey> CONTEXT = new ThreadLocal<>();

    public static void set(SecretKey aesKey) {
        CONTEXT.set(aesKey);
    }

    public static SecretKey get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
