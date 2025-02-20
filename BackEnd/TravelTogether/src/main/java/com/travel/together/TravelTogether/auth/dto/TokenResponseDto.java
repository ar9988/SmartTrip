package com.travel.together.TravelTogether.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenResponseDto {
    private Integer id;
    private String name;
    private String email;
    private String profile_image;
    private String profile;
    private String token;
}
