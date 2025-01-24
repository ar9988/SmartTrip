package com.travel.together.TravelTogether.websocket.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RouteData {
    private Integer id;
    private Integer tripId;
    private Integer dayId;
    private Integer orderNum;
    private int driveDuration;
    private Integer transDuration;


}
