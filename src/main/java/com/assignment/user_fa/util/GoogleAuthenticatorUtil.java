package com.assignment.user_fa.util;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;

/**
 * Utility class for handling operations related to Google Authenticator.
 * Used for enabling and verifying Time-based One-Time Passwords (TOTP) for 2FA.
 */
public class GoogleAuthenticatorUtil {

    // Singleton instance of GoogleAuthenticator used for generating and verifying TOTP codes
    private static final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    /**
     * Verifies the TOTP code provided by the user against the secret key.
     *
     * @param secret The base32 encoded secret key shared with the authenticator app
     * @param code   The 6-digit code provided by the user
     * @return true if the code is valid, false otherwise
     */
    public static boolean verifyCode(String secret, int code) {
        return gAuth.authorize(secret, code);
    }

    /**
     * Generates a new secret key to be shared with the user.
     * This key will be used by the Google Authenticator app to generate TOTP codes.
     *
     * @return A base32 encoded secret key
     */
    public static String generateSecret() {
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        return key.getKey();
    }
}
