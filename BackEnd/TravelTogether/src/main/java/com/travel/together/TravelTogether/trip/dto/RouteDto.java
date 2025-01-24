package com.travel.together.TravelTogether.trip.dto;

import com.travel.together.TravelTogether.trip.entity.Route;
import lombok.Getter;

@Getter
public class RouteDto {
    private int orderNum;
    private int driveDuration;
    private int transDuration;

    public RouteDto(Route route) {
        this.orderNum = route.getOrderNum();
        this.driveDuration = route.getDriveDuration();
        this.transDuration = route.getTransDuration();
    }

}
