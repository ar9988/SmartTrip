package com.travel.together.TravelTogether.aiPlanning.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DirectionsResponseDto {
    private Integer totalTime; // 총 소요 시간

    public DirectionsResponseDto(Integer totalTime) {
        this.totalTime = totalTime;
    }
}
