package com.travel.together.TravelTogether.trip.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TripDetailResponse {
    private Long tripId;
    private String title;
    private String creatorName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<ScheduleDto> schedules;
    private List<MemberDto> members;

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

    @Getter @Setter
    @NoArgsConstructor
    public static class MemberDto {
        private Long userId;
        private String nickname;
        private boolean isOwner;
    }

    //lombok 플러그인 설치 후 해결
//    public static TripDetailResponse from(Trip trip, List<Schedule> schedules, List<TripMember> members) {
//        TripDetailResponse response = new TripDetailResponse();
//        response.setTripId(trip.getId());
//        response.setTitle(trip.getTitle());
//        response.setCreatorName(trip.getCreator().getNickname());
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
//        List<MemberDto> memberDtos = members.stream()
//                .map(member -> {
//                    MemberDto dto = new MemberDto();
//                    dto.setUserId(member.getUser().getId());
//                    dto.setNickname(member.getUser().getNickname());
//                    dto.setOwner(member.isOwner());
//                    return dto;
//                })
//                .collect(Collectors.toList());
//
//        response.setSchedules(scheduleDtos);
//        response.setMembers(memberDtos);
//        return response;
//    }
}
