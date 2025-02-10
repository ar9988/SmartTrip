package com.travel.together.TravelTogether.tripwebsocket.dto;

import lombok.*;

import java.util.List;

// Path 정보를 담을 클래스
@Data
@AllArgsConstructor
public class PathInfo {
    private final Integer sourceScheduleId;
    private final Integer targetScheduleId;
    private final List<List<Double>> path;
    private Integer totalTime;

}