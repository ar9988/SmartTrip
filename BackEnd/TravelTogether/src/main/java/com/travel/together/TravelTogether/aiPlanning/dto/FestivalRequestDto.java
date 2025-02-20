package com.travel.together.TravelTogether.aiPlanning.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class FestivalRequestDto {
    private String startDate;
    private String endDate;
    private String regionNumber;

    public FestivalRequestDto(String startDate, String endDate, String regionNumber) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.regionNumber = regionNumber;
    }
}
