package com.assignment.user_fa.service.impl;

import com.assignment.user_fa.dto.*;
import com.assignment.user_fa.model.Session;
import com.assignment.user_fa.model.User;
import com.assignment.user_fa.repository.*;
import com.assignment.user_fa.service.AuthService;
import com.assignment.user_fa.service.EmailOtpService;
import com.assignment.user_fa.util.GoogleAuthenticatorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private SessionRepository sessionRepo;
    @Autowired
    private SessionHistoryRepository historyRepo;
    @Autowired
    private AuditLogRepository auditLogRepo;
    @Autowired
    private EmailOtpService emailOtpService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Handles user registration with email OTP verification
     */
    @Override
    public ResponseEntity<?> register(SignupRequest request) {
        try {
            // Check if user with given email already exists
            if (userRepo.findByEmail(request.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body("Email already in use");
            }

            // Create new user with encoded password
            User user = User.builder()
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .name(request.getName())
                    .designation(request.getDesignation())
                    .emailVerified(false)
                    .twoFactorEnabled(false)
                    .build();

            userRepo.save(user); // Save user to DB
            emailOtpService.sendOtp(user.getEmail()); // Send OTP for email verification

            return ResponseEntity.ok("User registered successfully. Please verify OTP.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Registration failed: " + e.getMessage());
        }
    }

    /**
     * Verifies email OTP and marks the email as verified
     */
    @Override
    public ResponseEntity<?> verifyEmailOtp(EmailOtpRequest request) {
        try {
            boolean valid = emailOtpService.verifyOtp(request.getEmail(), request.getOtp());
            if (!valid) return ResponseEntity.badRequest().body("Invalid or expired OTP");

            // Fetch user by email and update status
            User user = userRepo.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.setEmailVerified(true);
            userRepo.save(user);

            return ResponseEntity.ok("Email verified successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("OTP verification failed: " + e.getMessage());
        }
    }

    /**
     * Logs in the user and creates a new session token
     */
    @Override
    public ResponseEntity<?> login(LoginRequest request) {
        try {
            // Validate user credentials
            User user = userRepo.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Invalid email or password"));

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return ResponseEntity.badRequest().body("Invalid email or password");
            }

            if (!user.isEmailVerified()) {
                return ResponseEntity.badRequest().body("Email not verified");
            }

            // If 2FA is enabled, prompt to verify
            if (user.isTwoFactorEnabled()) {
                // Step 1: Create a temporary session with 2FA flag set to false
                String preAuthToken = UUID.randomUUID().toString();

                Session preSession = Session.builder()
                        .userId(user.getId())
                        .token(preAuthToken)
                        .expiresAt(LocalDateTime.now().plusMinutes(5)) // shorter expiry
                        .isVerified(false) // new field you’ll add
                        .build();
                sessionRepo.save(preSession);

                // Step 2: Return 202 + token
                return ResponseEntity.status(202).body(
                        Map.of(
                                "message", "2FA enabled. Please verify using /api/auth/verify-2fa",
                                "token", preAuthToken
                        )
                );
            }

            // Create session token valid for 15 minutes
            String token = UUID.randomUUID().toString();
            Session session = Session.builder()
                    .userId(user.getId())
                    .token(token)
                    .expiresAt(LocalDateTime.now().plusMinutes(15))
                    .build();
            sessionRepo.save(session);

            return ResponseEntity.ok(token); // Return session token
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Login failed: " + e.getMessage());
        }
    }

    /**
     * Retrieves profile information of the logged-in user
     */
    @Override
    public ResponseEntity<?> getProfile(String token) {
        try {
            Session session = sessionRepo.findByToken(token)
                    .orElseThrow(() -> new RuntimeException("Invalid session"));

            User user = userRepo.findById(session.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Build and return user profile
            ProfileGetResponse response = ProfileGetResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .designation(user.getDesignation())
                    .emailVerified(user.isEmailVerified())
                    .twoFactorEnabled(user.isTwoFactorEnabled())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching profile: " + e.getMessage());
        }
    }

    /**
     * Updates profile fields of the logged-in user
     */
    @Override
    public ResponseEntity<?> updateProfile(String token, ProfileUpdateRequest req) {
        try {
            Session session = sessionRepo.findByToken(token)
                    .orElseThrow(() -> new RuntimeException("Invalid session"));

            User user = userRepo.findById(session.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Update fields if present
            if (checkFieldPresent(req.getName())) {
                user.setName(req.getName());
            }
            if (checkFieldPresent(req.getDesignation())) {
                user.setDesignation(req.getDesignation());
            }

            userRepo.save(user); // Save updated user
            return ResponseEntity.ok("Profile updated successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Profile update failed: " + e.getMessage());
        }
    }

    /**
     * Sets up Two-Factor Authentication using Google Authenticator
     */
    @Override
    public ResponseEntity<?> setupTwoFactor(String token) {
        try {
            Session session = sessionRepo.findByToken(token)
                    .orElseThrow(() -> new RuntimeException("Invalid session"));

            User user = userRepo.findById(session.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // If no secret exists, generate one
            if (user.getTwoFactorSecret() == null) {
                String secret = GoogleAuthenticatorUtil.generateSecret();
                user.setTwoFactorEnabled(true);
                user.setTwoFactorSecret(secret);
                userRepo.save(user);
            }

            // Return the otpauth URI to scan with Authenticator app
            String otpAuthURL = "otpauth://totp/UserFAApp:" + user.getEmail() +
                    "?secret=" + user.getTwoFactorSecret() + "&issuer=UserFAApp";

            return ResponseEntity.ok().body("Setup this TOTP in your Google Authenticator: " + otpAuthURL);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("2FA setup failed: " + e.getMessage());
        }
    }

    /**
     * Verifies the 2FA code entered by the user
     */
    @Override
    public ResponseEntity<?> verifyTwoFactor(String token, int code) {
        try {
            Session session = sessionRepo.findByToken(token)
                    .orElseThrow(() -> new RuntimeException("Invalid session token"));

            if (Boolean.TRUE.equals(session.getIsVerified())) {
                return ResponseEntity.badRequest().body("Session already verified.");
            }

            User user = userRepo.findById(session.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!user.isTwoFactorEnabled() || user.getTwoFactorSecret() == null) {
                return ResponseEntity.badRequest().body("2FA not enabled for this user");
            }

            boolean valid = GoogleAuthenticatorUtil.verifyCode(user.getTwoFactorSecret(), code);
            if (!valid) {
                return ResponseEntity.badRequest().body("Invalid 2FA code");
            }

            session.setIsVerified(true);
            session.setExpiresAt(LocalDateTime.now().plusMinutes(30));
            sessionRepo.save(session);

            return ResponseEntity.ok(
                    Map.of(
                            "message", "2FA verified successfully",
                            "token", session.getToken()
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("2FA verification failed: " + e.getMessage());
        }
    }

    /**
     * Utility method to check if a field is non-null and non-empty
     */
    private boolean checkFieldPresent(String fieldValue) {
        return Objects.nonNull(fieldValue) && !fieldValue.isEmpty();
    }
}
