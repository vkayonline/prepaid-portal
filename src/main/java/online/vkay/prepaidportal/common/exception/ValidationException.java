package online.vkay.prepaidportal.common.exception;

import online.vkay.prepaidportal.common.enums.ResponseStatus;


/**
 * Used for invalid inputs, missing fields, or constraint violations.
 */
public class ValidationException extends BaseException {

    public ValidationException(String message) {
        super(ResponseStatus.GEN101, message);
    }

    public ValidationException(ResponseStatus status) {
        super(status);
    }

    public ValidationException(ResponseStatus status, String message) {
        super(status, message);
    }
}
