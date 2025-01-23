package com.travel.together.TravelTogether.trip.dto;

import com.travel.together.TravelTogether.trip.entity.Schedule;
import com.travel.together.TravelTogether.trip.entity.Trip;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class TripScheduleResponse_Backup {
    private Integer tripId;
    private String title;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<ScheduleDto> schedules;

    public Integer getTripId() {
        return tripId;
    }

    public void setTripId(Integer tripId) {
        this.tripId = tripId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public List<ScheduleDto> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<ScheduleDto> schedules) {
        this.schedules = schedules;
    }

    @Getter @Setter
    @NoArgsConstructor
    public static class ScheduleDto {
        private Integer id;
        private String placeName;
        private Integer day;
        private Integer order;
        private Double lat;
        private Double lng;
        private Integer type;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getPlaceName() {
            return placeName;
        }

        public void setPlaceName(String placeName) {
            this.placeName = placeName;
        }

        public Integer getDay() {
            return day;
        }

        public void setDay(Integer day) {
            this.day = day;
        }

        public Integer getOrder() {
            return order;
        }

        public void setOrder(Integer order) {
            this.order = order;
        }

        public Double getLat() {
            return lat;
        }

        public void setLat(Double lat) {
            this.lat = lat;
        }

        public Double getLng() {
            return lng;
        }

        public void setLng(Double lng) {
            this.lng = lng;
        }

        public Integer getType() {
            return type;
        }

        public void setType(Integer type) {
            this.type = type;
        }
    }

    public static TripScheduleResponse_Backup from(Trip trip, List<Schedule> schedules) {
        TripScheduleResponse_Backup response = new TripScheduleResponse_Backup();
        response.setTripId(trip.getId());
        response.setTitle(trip.getTitle());
        response.setStartDate(trip.getStartDate());
        response.setEndDate(trip.getEndDate());

//        List<ScheduleDto> scheduleDtos = schedules.stream()
//                .map(schedule -> {
//                    ScheduleDto dto = new ScheduleDto();
//                    dto.setId(schedule.getId());
//                    dto.setPlaceName(schedule.getPlaceName());
//                    dto.setDay(schedule.getDay());
//                    dto.setOrder(schedule.getOrderNum());
//                    dto.setLat(schedule.getLat());
//                    dto.setLng(schedule.getLng());
//                    dto.setType(schedule.getType());
//                    return dto;
//                })
//                .collect(Collectors.toList());
//
//        response.setSchedules(scheduleDtos);
        return response;
    }

}
