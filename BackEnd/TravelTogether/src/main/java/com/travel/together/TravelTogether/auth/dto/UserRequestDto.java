package com.travel.together.TravelTogether.auth.dto;

import lombok.Data;

@Data
public class UserRequestDto {
    private String email;
    private String password;
    private String name;
}
