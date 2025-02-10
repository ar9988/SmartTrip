package com.travel.together.TravelTogether.tripwebsocket.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AddRequest {
    // tripId, dayId, placeName, type,action
    private String action;
    private Integer tripId;
    private Integer dayId;
    private Integer type;   // 0번 점심, 1번 일정

}
