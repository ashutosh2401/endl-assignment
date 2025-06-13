package com.assignment.user_fa.service;

import com.assignment.user_fa.dto.EmailOtpRequest;
import com.assignment.user_fa.dto.LoginRequest;
import com.assignment.user_fa.dto.ProfileUpdateRequest;
import com.assignment.user_fa.dto.SignupRequest;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    ResponseEntity<?> register(SignupRequest request);
    ResponseEntity<?> verifyEmailOtp(EmailOtpRequest request);
    ResponseEntity<?> login(LoginRequest request);
    ResponseEntity<?> getProfile(String token);
    ResponseEntity<?> updateProfile(String token, ProfileUpdateRequest req);
    ResponseEntity<?> setupTwoFactor(String token);
    ResponseEntity<?> verifyTwoFactor(String token, int code);
}