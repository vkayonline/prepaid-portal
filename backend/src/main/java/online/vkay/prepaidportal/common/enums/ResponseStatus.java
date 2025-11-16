package online.vkay.prepaidportal.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ResponseStatus {

    // ==================================================
    // GENERIC
    // ==================================================
    GEN000("GEN000", true, "SUCCESS", HttpStatus.OK, "Success", "Request processed successfully."),
    GEN101("GEN101", false, "VALIDATION_ERROR", HttpStatus.BAD_REQUEST, "Validation Failed", "Invalid or missing request fields."),
    GEN202("GEN202", false, "UPSTREAM_FAILURE", HttpStatus.BAD_GATEWAY, "Upstream Failure", "Error communicating with external system."),
    GEN301("GEN301", false, "UNAUTHORIZED", HttpStatus.FORBIDDEN, "Access Denied", "You are not authorized to perform this operation."),
    GEN400("GEN400", false, "SYSTEM_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "System Error", "Unexpected internal or infrastructure error."),
    GEN901("GEN901", false, "DECRYPTION_FAILED", HttpStatus.BAD_REQUEST, "Decryption Failed", "Unable to decrypt the payload. Possibly malformed or invalid key."),
    GEN999("GEN999", false, "UNHANDLED_EXCEPTION", HttpStatus.INTERNAL_SERVER_ERROR, "Unhandled Exception", "Unexpected runtime exception occurred."),

    // ==================================================
    // AUTH MODULE
    // ==================================================
    AUTH000("AUTH000", true, "SUCCESS", HttpStatus.OK, "Login Successful", "User authenticated successfully."),
    AUTH101("AUTH101", false, "INVALID_CREDENTIALS", HttpStatus.BAD_REQUEST, "Invalid Credentials", "Username or password is incorrect."),
    AUTH202("AUTH202", false, "UPSTREAM_FAILURE", HttpStatus.BAD_GATEWAY, "Auth Provider Error", "Failed to contact external identity provider."),
    AUTH301("AUTH301", false, "UNAUTHORIZED", HttpStatus.UNAUTHORIZED, "Access Denied", "Invalid or expired session token."),
    AUTH400("AUTH400", false, "SYSTEM_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "Auth System Error", "Unexpected authentication system error."),
    AUTH999("AUTH999", false, "UNHANDLED_EXCEPTION", HttpStatus.INTERNAL_SERVER_ERROR, "Unhandled Auth Exception", "Unexpected authentication exception."),

    // ==================================================
    // FRM MODULE (Fraud & Risk)
    // ==================================================
    FRM000("FRM000", true, "SUCCESS", HttpStatus.OK, "Fraud Check Passed", "Fraud check completed successfully."),
    FRM101("FRM101", false, "VALIDATION_ERROR", HttpStatus.BAD_REQUEST, "Invalid FRM Input", "Invalid or missing risk parameters."),
    FRM202("FRM202", false, "UPSTREAM_FAILURE", HttpStatus.BAD_GATEWAY, "Scoring Engine Down", "Unable to reach external risk engine."),
    FRM301("FRM301", false, "FRAUD_DETECTED", HttpStatus.FORBIDDEN, "Fraud Detected", "Fraud rule triggered or risk limit exceeded."),
    FRM400("FRM400", false, "SYSTEM_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "FRM System Error", "Unexpected system error in risk evaluation."),
    FRM999("FRM999", false, "UNHANDLED_EXCEPTION", HttpStatus.INTERNAL_SERVER_ERROR, "Unhandled FRM Exception", "Unexpected runtime exception in FRM module.");

    /**
     * Alphanumeric response code, e.g., AUTH000, FRM301
     */
    private final String rc;

    /**
     * Whether the response represents a success case
     */
    private final boolean success;

    /**
     * Machine-readable business code (e.g., INVALID_CREDENTIALS)
     */
    private final String code;

    /**
     * Standardized HTTP status to use in ResponseEntity
     */
    private final HttpStatus httpStatus;

    /**
     * Short, user-facing title
     */
    private final String title;

    /**
     * Human-readable descriptive message
     */
    private final String description;
}
