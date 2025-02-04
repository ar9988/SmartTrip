package com.travel.together.TravelTogether.aiPlanning.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor  // 생성자 자동 생성
public class KakaoDto {
    private String placeName;  // 장소명
    private String address; // 주소
    private Double latitude;   // 위도
    private Double longitude;  // 경도
}
