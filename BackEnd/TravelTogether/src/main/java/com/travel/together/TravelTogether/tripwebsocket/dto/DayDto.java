package com.travel.together.TravelTogether.tripwebsocket.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class DayDto {
    private LocalDateTime startTime;
    private List<ScheduleDTO> schedules;

}
