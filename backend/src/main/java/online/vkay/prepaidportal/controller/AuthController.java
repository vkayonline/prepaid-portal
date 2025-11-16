package online.vkay.prepaidportal.controller;

import lombok.RequiredArgsConstructor;
import online.vkay.prepaidportal.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Check if SSO is enabled for the username.
     * Request: { "username": "user@example.com" }
     */
    @PostMapping("/check-sso")
    public ResponseEntity<?> checkSso(@RequestBody Map<String, String> req) {
        String username = req.get("username");
        boolean ssoEnabled = authService.isSsoEnabled(username);
        return ResponseEntity.ok(Map.of("ssoEnabled", ssoEnabled));
    }

    /**
     * Validate password. If no 2FA required, returns token.
     * Request: { "username":"...", "password":"..." }
     * Response: { "token": "..." } OR { "twoFaType": "TOTP" / "OTP" / "NONE", "fallback":"EMAIL_OTP" }
     */
    @PostMapping("/validate-password")
    public ResponseEntity<?> validatePassword(@RequestBody Map<String, String> req) {
        String username = req.get("username");
        String password = req.get("password");

        try {
            boolean valid = authService.validatePassword(username, password);
            if (!valid) return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));

            String twoFaType = authService.getActive2faType(username);
            if ("NONE".equalsIgnoreCase(twoFaType)) {
                String token = authService.generateToken(username);
                return ResponseEntity.ok(Map.of("token", token));
            }

            // Inform UI: prefer TOTP when available; email is fallback
            return ResponseEntity.ok(Map.of(
                    "twoFaType", twoFaType,
                    "fallback", "EMAIL_OTP"
            ));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(404).body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * Validate 2FA (TOTP primary, email fallback)
     * Request: { "username":"...", "code":"..." }
     * Response: { "token": "..." }
     */
    @PostMapping("/validate-2fa")
    public ResponseEntity<?> validate2fa(@RequestBody Map<String, String> req) {
        String username = req.get("username");
        String code = req.get("code");

        try {
            boolean ok = authService.validate2fa(username, code);
            if (!ok) {
                return ResponseEntity.status(401).body(Map.of(
                        "error", "Invalid code",
                        "message", "If you don't have TOTP, request email OTP fallback"
                ));
            }
            String token = authService.generateToken(username);
            return ResponseEntity.ok(Map.of("token", token));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(404).body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * Enable basic 2FA (email) for the user.
     * Request: { "username": "..." }
     */
    @PostMapping("/enable-2fa")
    public ResponseEntity<?> enableTwoFa(@RequestBody Map<String, String> req) {
        String username = req.get("username");
        authService.enableTwoFac(username);
        return ResponseEntity.ok(Map.of("message", "2FA enabled (email fallback active)."));
    }


    /**
     * Enable basic 2FA (email) for the user.
     * Request: { "username": "..." }
     */
    @PostMapping("/disable-2fa")
    public ResponseEntity<?> disableTwoFa(@RequestBody Map<String, String> req) {
        String username = req.get("username");
        authService.disableTwoFac(username);
        return ResponseEntity.ok(Map.of("message", "2FA enabled (email fallback active)."));
    }

    /**
     * Initiate TOTP setup (generate secret & QR)
     * Request: { "username":"..." }
     * Response: { "qrUrl": "otpauth://..." }
     */
    @PostMapping("/enable-totp/initiate")
    public ResponseEntity<?> initiateTotp(@RequestBody Map<String, String> req) {
        String username = req.get("username");
        String qrUrl = authService.initiateTotpSetup(username);
        return ResponseEntity.ok(Map.of(
                "message", "Scan this QR in your Authenticator app. Email remains fallback.",
                "qrUrl", qrUrl
        ));
    }

    /**
     * Confirm TOTP setup (user enters first code shown by authenticator)
     * Request: { "username":"...", "code":"..." }
     */
    @PostMapping("/enable-totp/confirm")
    public ResponseEntity<?> confirmTotp(@RequestBody Map<String, String> req) {
        String username = req.get("username");
        String code = req.get("code");

        boolean ok = authService.confirmTotpSetup(username, code);
        if (!ok) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid TOTP code. Try again."));
        }
        return ResponseEntity.ok(Map.of("message", "TOTP confirmed and activated. Email remains fallback."));
    }

    /**
     * Disable TOTP (revert to email-only 2FA)
     * Request: { "username":"..." }
     */
    @PostMapping("/disable-totp")
    public ResponseEntity<?> disableTotp(@RequestBody Map<String, String> req) {
        String username = req.get("username");
        authService.disableTotp(username);
        return ResponseEntity.ok(Map.of("message", "TOTP disabled. Email OTP remains active."));
    }
}



