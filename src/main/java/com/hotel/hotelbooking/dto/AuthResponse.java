package com.hotel.hotelbooking.dto;

import lombok.*;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String role;
    private String fullName;
    private String email;
}