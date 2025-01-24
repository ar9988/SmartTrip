package com.travel.together.TravelTogether.auth.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

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
    private List<String> createdTrips; // Trip 이름 또는 ID 리스트
    private List<String> tripMemberships; // 가입된 Trip 이름 또는 ID 리스트
    private List<String> photos; // 사진 이름 또는 경로 리스트
}