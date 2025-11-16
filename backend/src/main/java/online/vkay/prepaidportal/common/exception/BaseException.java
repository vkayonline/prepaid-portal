package online.vkay.prepaidportal.common.exception;

import lombok.Getter;
import online.vkay.prepaidportal.common.enums.ResponseStatus;

@Getter
public class BaseException extends RuntimeException {

    private final ResponseStatus status;

    public BaseException(ResponseStatus status) {
        super(status.getDescription());
        this.status = status;
    }

    public BaseException(ResponseStatus status, String customMessage) {
        super(customMessage);
        this.status = status;
    }
}
