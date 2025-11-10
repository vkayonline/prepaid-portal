package online.vkay.prepaidportal.service;

import lombok.RequiredArgsConstructor;
import online.vkay.prepaidportal.entity.UserLogin;
import online.vkay.prepaidportal.repository.UserLoginRepository;
import online.vkay.prepaidportal.utils.JwtUtil;
import online.vkay.prepaidportal.utils.PasswordUtil;
import online.vkay.prepaidportal.utils.TotpUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final UserLoginRepository userLoginRepository;

    /**
     * Check if SSO is enabled for username
     */
    public boolean isSsoEnabled(String username) {
        UserLogin login = userLoginRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return login.isSsoEnabled();
    }

    /**
     * Validate password (use hashed comparison in prod)
     */
    public boolean validatePassword(String username, String password) {
        UserLogin login = userLoginRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return PasswordUtil.matches(login.getPassword(), password);
    }

    /**
     * Determine whether 2FA is enabled for the user.
     * If totpSecret exists, we treat TOTP as available (primary) with email fallback.
     */
    public String getActive2faType(String username) {
        UserLogin login = userLoginRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!login.isTwoFaEnabled()) return TotpType.NONE.name();
        if (StringUtils.hasText(login.getTotpSecret())) return TotpType.TOTP.name();
        return TotpType.OTP.name();
    }

    /**
     * Validate 2FA code.
     * - Prefer TOTP if secret exists
     * - Fallback to email OTP if TOTP fails
     */
    public boolean validate2fa(String username, String code) {
        UserLogin login = userLoginRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!login.isTwoFaEnabled()) return true;

        boolean hasTotp = TotpType.TOTP.name().equals(login.getTwoFaType()) &&
                StringUtils.hasText(login.getTotpSecret());

        // Try TOTP first if configured as active type & secret present
        if (hasTotp) {
            boolean totpValid = TotpUtil.verifyCode(login.getTotpSecret(), code);
            if (totpValid) return true;
            // if TOTP fails, try email fallback
            // log for debugging
            System.out.println("TOTP invalid; trying email fallback");
        } else {
            // If twoFaType is not TOTP but totpSecret exists (your design: totpSecret can exist but email is default)
            boolean secretPresent = StringUtils.hasText(login.getTotpSecret());
            if (secretPresent) {
                boolean totpValid = TotpUtil.verifyCode(login.getTotpSecret(), code);
                if (totpValid) return true;
            }
        }

        // Fallback to email OTP
        // TODO: Replace with real OTP verification (Redis / DB / cache)
        return "123456".equals(code);
    }

    /**
     * Generate JWT token — passable claims map available if needed
     */
    public String generateToken(String username) {
        var login = userLoginRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return jwtUtil.generateToken(String.valueOf(login.getUser().getId()), Map.of());
    }

    /**
     * Enable 2FA (email becomes active/fallback)
     */
    public void enableTwoFac(String username) {
        UserLogin login = userLoginRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        login.setTwoFaEnabled(true);
        login.setTwoFaType(TotpType.OTP.name());
        userLoginRepository.save(login);
    }

    /**
     * Disable 2FA
     */
    public void disableTwoFac(String username) {
        UserLogin login = userLoginRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        login.setTwoFaEnabled(false);
        login.setTwoFaType(TotpType.NONE.name());
        login.setTotpSecret(null);
        userLoginRepository.save(login);
    }

    /**
     * Step 1: Start TOTP setup - generate secret & QR (same as enableTotpSetup, kept for clarity)
     */
    public String initiateTotpSetup(String username) {
        UserLogin login = userLoginRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!login.isTwoFaEnabled()) throw new RuntimeException("2F Not Enabled for this User");

        String secret = TotpUtil.generateSecret();
        login.setTotpSecret(secret);
        userLoginRepository.save(login);

        return TotpUtil.generateOtpAuthUri("Aditya Birla Capital Digital", username, secret);
    }

    /**
     * Step 2: Confirm TOTP setup by verifying first code and activating TOTP as the active type
     */
    public boolean confirmTotpSetup(String username, String code) {
        UserLogin login = userLoginRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String pendingSecret = login.getTotpSecret();
        if (pendingSecret == null) throw new RuntimeException("No TOTP secret found for user");

        boolean valid = TotpUtil.verifyCode(pendingSecret, code);
        if (valid) {
            login.setTwoFaEnabled(true);
            login.setTwoFaType(TotpType.TOTP.name());
            userLoginRepository.save(login);
        }

        return valid;
    }

    /**
     * Disable TOTP (revert to email-only 2FA)
     */
    public void disableTotp(String username) {
        UserLogin login = userLoginRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        login.setTotpSecret(null);
        login.setTwoFaType(TotpType.OTP.name());
        userLoginRepository.save(login);
    }

    private enum TotpType {
        OTP, TOTP, NONE
    }
}
