package com.travel.together.TravelTogether.aiPlanning.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FestivalDto {
    private String title; // 제목
    private String addr1; // 주소
    private String firstImage; // 이미지 주소
    private String eventStartDate; // 축제 시작 날짜
    private String eventEndDate; // 축제 종료 날짜
    private String contentid; // 콘텐츠 id
}
