package com.assignment.user_fa.repository;

import com.assignment.user_fa.model.EmailOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailOtpRepository extends JpaRepository<EmailOtp, Long> {
    Optional<EmailOtp> findByEmailAndOtp(String email, String otp);
    void deleteByEmail(String email);
}
