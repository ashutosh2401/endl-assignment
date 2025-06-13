package com.assignment.user_fa.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileGetResponse {
    private Long id;
    private String email;
    private String name;
    private String designation;
    private boolean emailVerified;
    private boolean twoFactorEnabled;
}