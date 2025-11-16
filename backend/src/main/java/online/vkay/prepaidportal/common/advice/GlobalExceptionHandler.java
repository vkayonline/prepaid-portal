package online.vkay.prepaidportal.common.advice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import online.vkay.prepaidportal.common.enums.ResponseStatus;
import online.vkay.prepaidportal.common.exception.BaseException;
import online.vkay.prepaidportal.dto.BaseApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;

/**
 * 🌐 Global Exception Handler
 * ----------------------------------------------------
 * Handles all exceptions in a consistent format.
 * Does NOT print stack trace (Aspect already does that).
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Handles all known categorized exceptions derived from BaseException.
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<BaseApiResponse<?>> handleBaseException(BaseException ex) {
        ResponseStatus status = ex.getStatus();

        // Compact, single-line structured log (no stack trace)
        log.warn("[{}:{}] {} - {}", status.getRc(), status.getCode(), status.getTitle(), ex.getMessage());

        BaseApiResponse<?> response = BaseApiResponse.failure(
                status,
                ex.getMessage(),
                List.of(BaseApiResponse.ErrorDetail.builder()
                        .message(ex.getMessage())
                        .reasonCode(status.getCode())
                        .build())
        );

        // Optional: pretty print for debug logs
        debugResponse(response);

        return ResponseEntity.status(status.getHttpStatus()).body(response);
    }

    /**
     * Handles all unclassified or unexpected exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseApiResponse<?>> handleUnhandledException(Exception ex) {
        ResponseStatus status = ResponseStatus.GEN999;

        // Log summary only (Aspect already printed stack trace)
        log.error("[{}:{}] {} - {}", status.getRc(), status.getCode(), status.getTitle(), ex.getMessage());

        BaseApiResponse<?> response = BaseApiResponse.failure(
                status,
                ex.getMessage(),
                List.of(BaseApiResponse.ErrorDetail.builder()
                        .message(ex.getMessage() != null ? ex.getMessage() : status.getDescription())
                        .reasonCode(status.getCode())
                        .build())
        );

        debugResponse(response);

        return ResponseEntity.status(status.getHttpStatus()).body(response);
    }

    /**
     * Optional: pretty-print response for debug logs
     */
    private void debugResponse(BaseApiResponse<?> response) {
        try {
            log.debug("API Response: {}", mapper.writeValueAsString(response));
        } catch (JsonProcessingException ignored) {
        }
    }
}
