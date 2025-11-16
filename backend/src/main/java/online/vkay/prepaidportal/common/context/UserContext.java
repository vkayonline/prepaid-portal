package online.vkay.prepaidportal.common.context;

import online.vkay.prepaidportal.entity.User;

/**
 * Simple thread-local security context to store authenticated user details
 * without using Spring Security.
 */
public class UserContext {

    private static final ThreadLocal<User> CONTEXT = new ThreadLocal<>();

    public static void setUser(User user) {
        CONTEXT.set(user);
    }

    public static User getUser() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
