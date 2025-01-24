package com.travel.together.TravelTogether.websocket.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ScheduleData {
    private Integer id;
    private Integer dayId;
    private Integer tripId;
    private String placeName;
    private Integer orderNum;
    private double lat;
    private double lng;
    private Integer type;
}
