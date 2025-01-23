package com.travel.together.TravelTogether.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponseDto {
    private Integer id;
    private String email;
    private String nickname;
    private String profile;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}