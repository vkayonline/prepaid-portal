package online.vkay.prepaidportal.common.exception;

import online.vkay.prepaidportal.common.enums.ResponseStatus;

/**
 * Category 3 (Access / Authorization / Fraud)
 * Includes auth failures, access denials, and fraud rule triggers.
 */
public class AccessException extends BaseException {

    public AccessException(String message) {
        super(ResponseStatus.GEN301, message);
    }

    public AccessException(ResponseStatus status) {
        super(status);
    }

    public AccessException(ResponseStatus status, String message) {
        super(status, message);
    }
}
