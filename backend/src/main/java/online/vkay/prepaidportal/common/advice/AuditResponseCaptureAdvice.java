package online.vkay.prepaidportal.common.advice;

import online.vkay.prepaidportal.common.context.AuditContext;
import online.vkay.prepaidportal.dto.BaseApiResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Intercepts all controller responses before serialization/encryption.
 * Used to extract rc/message for audit logging.
 */
@ControllerAdvice
public class AuditResponseCaptureAdvice implements ResponseBodyAdvice<BaseApiResponse> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // Apply only to controllers returning BaseApiResponse or its subclasses
        return BaseApiResponse.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public BaseApiResponse beforeBodyWrite(BaseApiResponse body,
                                           MethodParameter returnType,
                                           MediaType selectedContentType,
                                           Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                           ServerHttpRequest request,
                                           ServerHttpResponse response) {

        if (body != null) {
            AuditContext.set(AuditContext.builder()
                    .rc(body.getRc())
                    .message(body.getDescription())
                    .build());
        }
        return body;
    }
}
