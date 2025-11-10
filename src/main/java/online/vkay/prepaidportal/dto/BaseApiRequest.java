package online.vkay.prepaidportal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BaseApiRequest<T> {

    /**
     * Information about the device or channel (mobile/web/system).
     * Useful for fraud, analytics, or multi-channel traceability.
     */
    private Device device;

    /**
     * Actual business payload (dynamic based on API).
     */
    private T payload;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Device {
        private String deviceId;
        private String deviceType;
        private String manufacturer;
        private String os;
        private String osVersion;
        private String appVersion;
        private String ipAddress;
        private String channel;
    }
}

