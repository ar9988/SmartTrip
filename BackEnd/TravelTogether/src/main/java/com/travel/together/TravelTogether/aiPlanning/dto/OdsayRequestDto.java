package com.travel.together.TravelTogether.aiPlanning.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class OdsayRequestDto {
    private Double startLongitude; //시작 경도
    private Double startLatitude;  //시작 위도
    private Double endLongitude; //도착 경도
    private Double endLatitude; //도착 위도
}
