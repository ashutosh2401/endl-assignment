package com.assignment.user_fa.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequest {
    private String name;
    private String designation;
}
