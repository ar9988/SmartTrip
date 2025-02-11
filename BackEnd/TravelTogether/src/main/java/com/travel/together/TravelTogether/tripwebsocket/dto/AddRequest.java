package com.travel.together.TravelTogether.tripwebsocket.dto;

import com.travel.together.TravelTogether.trip.dto.ScheduleDto;
import com.travel.together.TravelTogether.trip.entity.Schedule;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AddRequest {
    private String action;  // EDIT, ADD
    private Integer tripId;
    private Integer dayId;
    private ScheduleDto schedule;
    private Long timestamp;


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
