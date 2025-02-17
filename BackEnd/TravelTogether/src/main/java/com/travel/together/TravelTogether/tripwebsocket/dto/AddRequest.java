package com.travel.together.TravelTogether.tripwebsocket.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class    AddRequest {
    private String action;  // EDIT, ADD
    private Integer tripId;
    private Integer dayOrder;
    private ScheduleDto schedule;
    private Long timeStamp;


    @Setter
    @Getter
    @NoArgsConstructor
    public static class ScheduleDto  {
        private Integer scheduleId;
        private String placeName;
        private Integer orderNum;
        private Double lat;
        private Double lng;
        private Integer type;
        private Integer positionPath;
        private Integer duration;
    }
}
