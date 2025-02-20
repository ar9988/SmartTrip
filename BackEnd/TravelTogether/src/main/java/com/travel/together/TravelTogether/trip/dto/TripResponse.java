package com.travel.together.TravelTogether.trip.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.travel.together.TravelTogether.trip.entity.Trip;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class TripResponse {
    private List<TripDto> trips;

    public List<TripDto> getTrips() {
        return trips;
    }

    public void setTrips(List<TripDto> trips) {
        this.trips = trips;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class TripDto {
        private Integer tripId;
        private String title;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
        private LocalDateTime startDate;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
        private LocalDateTime endDate;
        private String thumbnail;
        private Integer memberCount;
        private Boolean status;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
        private LocalDateTime createdAt;


        //getter and setter

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

        public String getThumbnail() {
            return thumbnail;
        }

        public void setThumbnail(String thumbnail) {
            this.thumbnail = thumbnail;
        }

        public Integer getMemberCount() {
            return memberCount;
        }

        public void setMemberCount(Integer memberCount) {
            this.memberCount = memberCount;
        }

        public Boolean getStatus() {
            return status;
        }

        public void setStatus(Boolean status) {
            this.status = status;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }

    public static TripResponse from(
            List<Trip> trips,
            Function<Trip, Integer> memberCounter,
            Function<Trip, String> thumbnailGetter,
            Function<Trip, Boolean> statusChecker) {

        List<TripDto> tripDtos = trips.stream()
                .map(trip -> {
                    TripDto dto = new TripDto();
                    dto.setTripId(trip.getId());
                    dto.setTitle(trip.getTitle());
                    dto.setStartDate(trip.getStartDate());
                    dto.setEndDate(trip.getEndDate());
                    dto.setThumbnail(thumbnailGetter.apply(trip));
                    dto.setMemberCount(memberCounter.apply(trip));
                    dto.setStatus(statusChecker.apply(trip));
                    dto.setCreatedAt(trip.getCreatedAt());
                    return dto;
                })
                .collect(Collectors.toList());

        TripResponse response = new TripResponse();
        response.setTrips(tripDtos);
        return response;
    }
}