package com.travel.together.TravelTogether.auth.dto;

public class AuthResponseDTO {
    private String email;
    private String token;

    public AuthResponseDTO(String email, String token) {
        this.email = email;
        this.token = token;
    }

    public String getEmail() { return email; }
    public String getToken() { return token; }
}
