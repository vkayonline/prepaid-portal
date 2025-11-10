package online.vkay.prepaidportal.common.context;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuditContext {
    private String rc;
    private String message;

    private static final ThreadLocal<AuditContext> CONTEXT = new ThreadLocal<>();

    public static void set(AuditContext context) {
        CONTEXT.set(context);
    }

    public static AuditContext get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
