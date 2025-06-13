package com.assignment.user_fa.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailOtpRequest {
    public String email;
    public String otp;
}