package com.travel.together.TravelTogether.tripwebsocket.dto;

import java.util.List;

public class DirectionsPathDto {
    private List<List<Double>> path;
    private Integer totalTime;

    public DirectionsPathDto(List<List<Double>> path, Integer totalTime) {
        this.path = path;
        this.totalTime = totalTime;
    }

}
