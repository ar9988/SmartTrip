package com.travel.together.TravelTogether.aiPlanning.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class KakaoResponseDto {
    private List<KakaoDto> places; //리스트 형태의 자료

    public KakaoResponseDto(List<KakaoDto> places) {
        this.places = places;
    }
}
