package com.travel.together.TravelTogether.trip.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TripScheduleResponse {
    private Long tripId;
    private String title;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<ScheduleDto> schedules;

    @Getter @Setter
    @NoArgsConstructor
    public static class ScheduleDto {
        private Long id;
        private String placeName;
        private Integer day;
        private Integer order;
        private Double lat;
        private Double lng;
        private Integer type;
    }

//lombok플러그인 설치후에 해결..
//    public static TripScheduleResponse from(Trip trip, List<Schedule> schedules) {
//        TripScheduleResponse response = new TripScheduleResponse();
//        response.setTripId(trip.getId());
//        response.setTitle(trip.getTitle());
//        response.setStartDate(trip.getStartDate());
//        response.setEndDate(trip.getEndDate());
//
//        List<ScheduleDto> scheduleDtos = schedules.stream()
//                .map(schedule -> {
//                    ScheduleDto dto = new ScheduleDto();
//                    dto.setId(schedule.getId());
//                    dto.setPlaceName(schedule.getPlaceName());
//                    dto.setDay(schedule.getDay());
//                    dto.setOrder(schedule.getOrder());
//                    dto.setLat(schedule.getLat());
//                    dto.setLng(schedule.getLng());
//                    dto.setType(schedule.getType());
//                    return dto;
//                })
//                .collect(Collectors.toList());
//
//        response.setSchedules(scheduleDtos);
//        return response;
    }
}
