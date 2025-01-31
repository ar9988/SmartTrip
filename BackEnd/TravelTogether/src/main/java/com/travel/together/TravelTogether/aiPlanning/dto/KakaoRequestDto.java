package com.travel.together.TravelTogether.aiPlanning.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor  // 생성자 자동 생성
public class KakaoRequestDto {
    private String keyword;  // 저장된 키워드
}
