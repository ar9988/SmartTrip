package com.travel.together.TravelTogether.aiPlanning.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class FestivalDto {
    private String title; // 제목
    private String address; // 주소
    private String image; // 이미지 주소
    private String eventStartDate; // 축제 시작 날짜
    private String eventEndDate; // 축제 종료 날짜
    private String contentid; // 콘텐츠 id
}
