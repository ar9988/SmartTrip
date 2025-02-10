package com.travel.together.TravelTogether.tripwebsocket.dto;


import com.travel.together.TravelTogether.tripwebsocket.service.TripStateManager;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MoveResponse {
    private final Integer tripId;
    private final Integer scheduleId;
    private final Integer newPosition;
    private final List<PathInfo> paths;

    public MoveResponse(Integer tripId, Integer scheduleId, Integer newPosition, List<PathInfo> paths) {
        this.tripId = tripId;
        this.scheduleId = scheduleId;
        this.newPosition = newPosition;
        this.paths = paths;
    }

}
