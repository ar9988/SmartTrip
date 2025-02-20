package com.travel.together.TravelTogether.aiPlanning.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class FestivalResponseDto {
    private List<FestivalDto> festivals; //리스트 형태의 자료

    public FestivalResponseDto(List<FestivalDto> festivals) {
        this.festivals = festivals;
    }
}
