package com.travel.together.TravelTogether.aiPlanning.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DirectionsRequestDto {
    private Double startLongitude; //시작 경도
    private Double startLatitude;  //시작 위도
    private Double endLongitude; //도착 경도
    private Double endLatitude; //도착 위도

    public DirectionsRequestDto(Double startLongitude, Double startLatitude, Double endLongitude, Double endLatitude) {
        this.startLongitude = startLongitude;
        this.startLatitude = startLatitude;
        this.endLongitude = endLongitude;
        this.endLatitude = endLatitude;
    }
}
