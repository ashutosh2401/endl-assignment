package com.assignment.user_fa.util;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;

public class GoogleAuthenticatorUtil {
    private static final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    public static boolean verifyCode(String secret, int code) {
        return gAuth.authorize(secret, code);
    }

    public static String generateSecret() {
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        return key.getKey();
    }
}
