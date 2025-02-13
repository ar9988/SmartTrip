package com.travel.together.TravelTogether.aiPlanning.dto;

import lombok.*;

@Data
public class TravelingSpotDto {
    private String region; // 지역 (서울특별시, 경상북도 등)
    private String regionNumber; // 지역 코드
    private String contentId; // 장소의 고유 id값
    private String title; // 장소명
    private String overView; // 장소에 대한 설명
    private String address; // 장소의 주소
    private String imageUrl; // 장소의 이미지 url
}
