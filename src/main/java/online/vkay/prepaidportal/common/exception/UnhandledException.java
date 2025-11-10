package online.vkay.prepaidportal.common.exception;

import online.vkay.prepaidportal.common.enums.ResponseStatus;

/**
 * Category 9 (Unhandled / Unknown)
 * Catch-all for unclassified or unexpected runtime exceptions.
 */
public class UnhandledException extends BaseException {

    public UnhandledException(Throwable cause) {
        super(ResponseStatus.GEN999, cause.getMessage());
    }

    public UnhandledException(String message) {
        super(ResponseStatus.GEN999, message);
    }

    public UnhandledException(ResponseStatus status, String message) {
        super(status, message);
    }
}
