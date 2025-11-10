package online.vkay.prepaidportal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import online.vkay.prepaidportal.common.enums.ResponseStatus;
import org.apache.commons.lang3.StringUtils;


import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BaseApiResponse<T> {

    /**
     * Boolean indicator of overall API call success.
     * true  → SUCCESS family codes (rc ending with 000-099)
     * false → any ERROR family codes (rc >= 100)
     */
    private boolean success;

    /**
     * Alphanumeric response code (module + category + index)
     * e.g., AUTH000, AUTH101, GEN999
     */
    private String rc;

    /**
     * Human-readable code used by developers/logs.
     * e.g., "SUCCESS", "INVALID_CREDENTIALS"
     */
    private String code;

    /**
     * Short UI-friendly heading.
     * e.g., "Login Failed", "Two-Factor Authentication"
     */
    private String title;

    /**
     * Long UI-friendly message.
     * e.g., "Your username or password is incorrect."
     */
    private String description;

    /**
     * Data payload returned on success.
     */
    private T data;

    /**
     * Detailed error list returned on failure.
     */
    private List<ErrorDetail> errors;

    // ======== Factory Methods ========
    public static <T> BaseApiResponse<T> success(ResponseStatus status, T data) {
        return BaseApiResponse.<T>builder()
                .success(status.isSuccess())
                .rc(status.getRc())
                .code(status.getCode())
                .title(status.getTitle())
                .description(status.getDescription())
                .data(data)
                .build();
    }

    public static <T> BaseApiResponse<T> success(ResponseStatus status, String customMessage, T data) {
        return BaseApiResponse.<T>builder()
                .success(status.isSuccess())
                .rc(status.getRc())
                .code(status.getCode())
                .title(status.getTitle())
                .description(StringUtils.defaultIfEmpty(customMessage, status.getDescription()))
                .data(data)
                .build();
    }

    public static <T> BaseApiResponse<T> failure(ResponseStatus status, String customMessage, List<ErrorDetail> errors) {
        return BaseApiResponse.<T>builder()
                .success(status.isSuccess())
                .rc(status.getRc())
                .code(status.getCode())
                .title(status.getTitle())
                .description(StringUtils.defaultIfEmpty(customMessage, status.getDescription()))
                .errors(errors)
                .build();
    }

    public static <T> BaseApiResponse<T> failure(ResponseStatus status, String customMessage) {
        return failure(status, customMessage, List.of(ErrorDetail.builder()
                .message(StringUtils.defaultIfEmpty(customMessage, status.getDescription()))
                .reasonCode(status.getCode())
                .build()));
    }

    // ======== Nested Class ========

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ErrorDetail {
        private String field;       // field name that failed validation
        private String message;     // error description
        private String reasonCode;  // internal reason code
    }
}
