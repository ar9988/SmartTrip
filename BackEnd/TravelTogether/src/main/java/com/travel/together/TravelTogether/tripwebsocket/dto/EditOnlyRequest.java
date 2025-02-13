package com.travel.together.TravelTogether.tripwebsocket.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EditOnlyRequest {
    private String action;
    private int dayOrder;
    private Schedule schedule;
    private long timeStamp;
    private int tripId;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Schedule {
        private int day;
        private int duration;
        private int id;
        private double lat;
        private double lng;
        private String placeName;
        private int positionPath;
        private int tripId;
        private int type;
    }
}
