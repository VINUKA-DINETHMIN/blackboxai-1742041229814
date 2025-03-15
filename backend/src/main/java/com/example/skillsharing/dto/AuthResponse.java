package com.example.skillsharing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private UserDTO user;

    public AuthResponse(String accessToken) {
        this.accessToken = accessToken;
    }

    public AuthResponse(String accessToken, UserDTO user) {
        this.accessToken = accessToken;
        this.user = user;
    }
}
