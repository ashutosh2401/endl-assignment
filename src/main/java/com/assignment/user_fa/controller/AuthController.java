package com.assignment.user_fa.controller;

import com.assignment.user_fa.dto.EmailOtpRequest;
import com.assignment.user_fa.dto.LoginRequest;
import com.assignment.user_fa.dto.ProfileUpdateRequest;
import com.assignment.user_fa.dto.SignupRequest;
import com.assignment.user_fa.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest req) {
        return authService.register(req);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody EmailOtpRequest req) {
        return authService.verifyEmailOtp(req);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String token) {
        return authService.getProfile(token);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestHeader("Authorization") String token,
                                           @RequestBody ProfileUpdateRequest req) {
        return authService.updateProfile(token, req);
    }

    @PostMapping("/enable-2fa")
    public ResponseEntity<?> enable2FA(@RequestHeader("Authorization") String token) {
        return authService.setupTwoFactor(token);
    }

    @PostMapping("/verify-2fa")
    public ResponseEntity<?> verifyTwoFactor(@RequestHeader("Authorization") String token,
                                             @RequestParam int code) {
        return authService.verifyTwoFactor(token, code);
    }
}
