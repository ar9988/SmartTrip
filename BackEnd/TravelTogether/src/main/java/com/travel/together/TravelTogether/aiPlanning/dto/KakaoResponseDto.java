package com.travel.together.TravelTogether.aiPlanning.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor  // 생성자 자동 생성
public class KakaoResponseDto {
    private String placeName;  // 장소명
    private Double latitude;   // 위도
    private Double longitude;  // 경도
}
