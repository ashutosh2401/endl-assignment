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


    @Override
    public ResponseEntity<?> register(SignupRequest request) {
        if (userRepo.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already in use");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .designation(request.getDesignation())
                .emailVerified(false)
                .twoFactorEnabled(false)
                .build();
        userRepo.save(user);

        emailOtpService.sendOtp(user.getEmail());

        return ResponseEntity.ok("User registered successfully. Please verify OTP.");
    }


    @Override
    public ResponseEntity<?> verifyEmailOtp(EmailOtpRequest request) {
        boolean valid = emailOtpService.verifyOtp(request.getEmail(), request.getOtp());
        if (!valid) return ResponseEntity.badRequest().body("Invalid or expired OTP");

        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEmailVerified(true);
        userRepo.save(user);

        return ResponseEntity.ok("Email verified successfully");
    }


    @Override
    public ResponseEntity<?> login(LoginRequest request) {
        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("Invalid email or password");
        }

        if (!user.isEmailVerified()) {
            return ResponseEntity.badRequest().body("Email not verified");
        }

        if (user.isTwoFactorEnabled()) {
            return ResponseEntity.status(202).body("2FA enabled. Please verify with /api/auth/verify-2fa");
        }

        String token = UUID.randomUUID().toString();
        Session session = Session.builder()
                .userId(user.getId())
                .token(token)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();
        sessionRepo.save(session);

        return ResponseEntity.ok(token);
    }


    @Override
    public ResponseEntity<?> getProfile(String token) {
        Session session = sessionRepo.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid session"));
        User user = userRepo.findById(session.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        ProfileGetResponse response =
                ProfileGetResponse.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .name(user.getName())
                        .designation(user.getName())
                        .emailVerified(user.isEmailVerified())
                        .twoFactorEnabled(user.isTwoFactorEnabled())
                        .build();
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<?> updateProfile(String token, ProfileUpdateRequest req) {
        Session session = sessionRepo.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid session"));
        User user = userRepo.findById(session.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if(checkFieldPresent(req.getName())) {
            user.setName(req.getName());
        }
        if(checkFieldPresent(req.getDesignation())){
            user.setDesignation(req.getDesignation());
        }
        userRepo.save(user);
        return ResponseEntity.ok("Profile updated successfully");
    }

    @Override
    public ResponseEntity<?> setupTwoFactor(String token) {
        Session session = sessionRepo.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid session"));
        User user = userRepo.findById(session.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getTwoFactorSecret() == null) {
            String secret = GoogleAuthenticatorUtil.generateSecret();
            user.setTwoFactorEnabled(true);
            user.setTwoFactorSecret(secret);
            userRepo.save(user);
        }

        String otpAuthURL = "otpauth://totp/UserFAApp:" + user.getEmail() +
                "?secret=" + user.getTwoFactorSecret() + "&issuer=UserFAApp";

        return ResponseEntity.ok().body("Setup this TOTP in your Google Authenticator: " + otpAuthURL);
    }

    @Override
    public ResponseEntity<?> verifyTwoFactor(String token, int code) {
        Session session = sessionRepo.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid session"));
        User user = userRepo.findById(session.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isTwoFactorEnabled() || user.getTwoFactorSecret() == null) {
            return ResponseEntity.badRequest().body("2FA not enabled for this user");
        }

        boolean valid = GoogleAuthenticatorUtil.verifyCode(user.getTwoFactorSecret(), code);
        if (!valid) return ResponseEntity.badRequest().body("Invalid 2FA code");

        return ResponseEntity.ok("2FA verification successful");
    }

    private boolean checkFieldPresent(String fieldValue) {
        return Objects.nonNull(fieldValue) && !fieldValue.isEmpty();
    }
}
