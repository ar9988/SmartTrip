package com.travel.together.TravelTogether.tripwebsocket.dto;

import com.travel.together.TravelTogether.trip.entity.Schedule;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AddRequest {
    // tripId, dayId, placeName, type,action
    private String action;
    private List<Schedule> Schedule;

}
