package online.vkay.prepaidportal.common.exception;

import online.vkay.prepaidportal.common.enums.ResponseStatus;

/**
 * Category 2 (Upstream / Integration)
 * Used when dependent system or external API fails.
 */
public class UpstreamException extends BaseException {

    public UpstreamException(String message) {
        super(ResponseStatus.GEN202, message);
    }

    public UpstreamException(ResponseStatus status) {
        super(status);
    }

    public UpstreamException(ResponseStatus status, String message) {
        super(status, message);
    }
}

