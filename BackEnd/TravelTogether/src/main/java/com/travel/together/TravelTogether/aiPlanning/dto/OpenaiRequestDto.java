package com.travel.together.TravelTogether.aiPlanning.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class OpenaiRequestDto {
    private String userId;
    private String startDate;
    private String startTime;
    private String endDate;
    private String endTime;
    private List<String> destination;
    private Preferences preferences;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Preferences {
        private List<String> places;
        private List<String> theme;
    }
}
