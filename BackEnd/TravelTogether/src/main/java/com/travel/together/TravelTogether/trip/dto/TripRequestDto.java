package com.travel.together.TravelTogether.trip.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class TripRequestDto {
    private Integer userId;
    private String title;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
