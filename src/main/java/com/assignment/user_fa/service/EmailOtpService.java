package com.assignment.user_fa.service;

import com.assignment.user_fa.model.EmailOtp;
import com.assignment.user_fa.repository.EmailOtpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class EmailOtpService {

    @Autowired
    private EmailOtpRepository otpRepository;

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Generates and sends a 6-digit OTP to the specified email address.
     * Stores the OTP with an expiry of 10 minutes in the database.
     */
    public void sendOtp(String email) {
        // Generate a random 6-digit OTP
        String otp = String.valueOf(new Random().nextInt(999999));

        // Create and store the OTP with expiry time
        EmailOtp emailOtp = new EmailOtp();
        emailOtp.setEmail(email);
        emailOtp.setOtp(otp);
        emailOtp.setExpiry(LocalDateTime.now().plusMinutes(10)); // OTP valid for 10 minutes
        otpRepository.save(emailOtp);

        // Send the OTP via email
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("Your OTP Code");
        msg.setText("Your OTP is: " + otp);
        mailSender.send(msg);
    }

    /**
     * Verifies if the given OTP for the email is valid and not expired.
     *
     * @param email the email to check against
     * @param otp   the OTP to verify
     * @return true if valid and not expired, false otherwise
     */
    public boolean verifyOtp(String email, String otp) {
        // Check if OTP exists and has not expired
        return otpRepository.findByEmailAndOtp(email, otp)
                .filter(e -> e.getExpiry().isAfter(LocalDateTime.now()))
                .isPresent();
    }
}
