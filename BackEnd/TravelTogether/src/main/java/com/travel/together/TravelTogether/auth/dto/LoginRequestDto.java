package com.travel.together.TravelTogether.auth.dto;

import lombok.Data;

@Data
public class LoginRequestDto {
    private String email;
    private String password;
}
