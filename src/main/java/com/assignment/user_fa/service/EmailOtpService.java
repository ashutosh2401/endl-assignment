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

    public void sendOtp(String email) {
        String otp = String.valueOf(new Random().nextInt(999999));
        EmailOtp emailOtp = new EmailOtp();
        emailOtp.setEmail(email);
        emailOtp.setOtp(otp);
        emailOtp.setExpiry(LocalDateTime.now().plusMinutes(10));
        otpRepository.save(emailOtp);

        // send email logic
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("Your OTP Code");
        msg.setText("Your OTP is: " + otp);
        mailSender.send(msg);
    }

    public boolean verifyOtp(String email, String otp) {
        return otpRepository.findByEmailAndOtp(email, otp)
                .filter(e -> e.getExpiry().isAfter(LocalDateTime.now()))
                .isPresent();
    }
}