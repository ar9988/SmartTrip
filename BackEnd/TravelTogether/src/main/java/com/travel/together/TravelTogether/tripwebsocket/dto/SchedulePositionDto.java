package com.travel.together.TravelTogether.tripwebsocket.dto;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SchedulePositionDto {
    @Id
    private Integer id;
    private Integer positionPath;

}
