package com.travel.together.TravelTogether.aiPlanning.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DirectionsRequestDto {
    private String startLongitude; //시작 경도
    private String startLatitude;  //시작 위도
    private String endLongitude; //도착 경도
    private String endLatitude; //도착 위도

    public DirectionsRequestDto(String startLongitude, String startLatitude, String endLongitude, String endLatitude) {
        this.startLongitude = startLongitude;
        this.startLatitude = startLatitude;
        this.endLongitude = endLongitude;
        this.endLatitude = endLatitude;
    }
}
