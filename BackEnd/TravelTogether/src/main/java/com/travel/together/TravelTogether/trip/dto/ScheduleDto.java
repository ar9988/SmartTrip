package com.travel.together.TravelTogether.trip.dto;


import com.travel.together.TravelTogether.trip.entity.Schedule;
import lombok.Getter;

@Getter
public class ScheduleDto {
    private String placeName;
    private int orderNum;
    private double lat;
    private double lng;
    private int type;

    public ScheduleDto(Schedule schedule) {
        this.placeName = schedule.getPlaceName();
        this.orderNum = schedule.getOrderNum();
        this.lat = schedule.getLat();
        this.lng = schedule.getLng();
        this.type = schedule.getType();
    }
}
