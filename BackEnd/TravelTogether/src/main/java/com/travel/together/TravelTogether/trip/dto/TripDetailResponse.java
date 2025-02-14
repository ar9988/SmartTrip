package com.travel.together.TravelTogether.trip.dto;

import com.travel.together.TravelTogether.trip.entity.Trip;
import com.travel.together.TravelTogether.trip.entity.TripMember;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class TripDetailResponse {
    private Integer tripId;
    private String title;
    private List<MemberDto> members;
    private List<DayDto> day;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TripDetailResponse(Trip trip, List<TripMember> members, List<DayDto> days) {
        this.tripId = trip.getId();
        this.title = trip.getTitle();
        this.members = members.stream()
                .map(MemberDto::new)
                .collect(Collectors.toList());
        this.day = days;
        this.createdAt = trip.getCreatedAt();
        this.updatedAt = trip.getUpdatedAt();
    }

}
