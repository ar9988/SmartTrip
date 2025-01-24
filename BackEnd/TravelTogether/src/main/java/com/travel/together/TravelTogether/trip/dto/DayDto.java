package com.travel.together.TravelTogether.trip.dto;

import com.travel.together.TravelTogether.trip.entity.Day;
import com.travel.together.TravelTogether.trip.entity.Route;
import com.travel.together.TravelTogether.trip.entity.Schedule;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class DayDto {
    private LocalDateTime startTime;
    private List<ScheduleDto> schedule;
    private List<RouteDto> route;

    public DayDto(Day day, List<Schedule> schedules, List<Route> routes) {
        this.startTime = day.getStartTime();
        this.schedule = schedules.stream()
                .map(ScheduleDto::new)
                .collect(Collectors.toList());
        this.route = routes.stream()
                .map(RouteDto::new)
                .collect(Collectors.toList());
    }
}
