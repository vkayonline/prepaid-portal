package online.vkay.prepaidportal.common.exception;

import online.vkay.prepaidportal.common.enums.ResponseStatus;

/**
 * Category 4 (System / Technical)
 * Internal errors such as DB failures, configuration issues, or runtime exceptions.
 */
public class SystemException extends BaseException {

    public SystemException(String message) {
        super(ResponseStatus.GEN400, message);
    }

    public SystemException(ResponseStatus status) {
        super(status);
    }

    public SystemException(ResponseStatus status, String message) {
        super(status, message);
    }
}

