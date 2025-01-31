package com.travel.together.TravelTogether.aiPlanning.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FestivalRequestDto {
    private String startDate;
    private String endDate;
    private String regionNumber;
}
