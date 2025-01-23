package com.travel.together.TravelTogether.auth.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class UserResponseDto {
    private Integer id;
    private String email;
    private String nickname;
    private String profile;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}