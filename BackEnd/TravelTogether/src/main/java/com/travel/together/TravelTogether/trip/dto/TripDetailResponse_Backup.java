package com.travel.together.TravelTogether.trip.dto;

import com.travel.together.TravelTogether.trip.entity.Schedule;
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
public class TripDetailResponse_Backup {
    private Integer tripId;
    private String title;
    private String creatorName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<ScheduleDto> schedules;
    private List<MemberDto> members;
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

    @Getter @Setter
    @NoArgsConstructor
    public static class MemberDto {
        private Integer userId;
        private String nickname;
        private boolean isOwner;

        public Integer getUserId() {
            return userId;
        }

        public void setUserId(Integer userId) {
            this.userId = userId;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public boolean isOwner() {
            return isOwner;
        }

        public void setOwner(boolean owner) {
            isOwner = owner;
        }
    }

    public static TripDetailResponse_Backup from(Trip trip, List<Schedule> schedules, List<TripMember> members) {
        TripDetailResponse_Backup response = new TripDetailResponse_Backup();
        response.setTripId(trip.getId());
        response.setTitle(trip.getTitle());
        response.setCreatorName(trip.getCreator().getNickname());
        response.setStartDate(trip.getStartDate());
        response.setEndDate(trip.getEndDate());

        List<ScheduleDto> scheduleDtos = schedules.stream()
                .map(schedule -> {
                    ScheduleDto dto = new ScheduleDto();
                    dto.setId(schedule.getId());
                    dto.setPlaceName(schedule.getPlaceName());
                    dto.setOrder(schedule.getOrderNum());
                    dto.setLat(schedule.getLat());
                    dto.setLng(schedule.getLng());
                    dto.setType(schedule.getType());
                    return dto;
                })
                .collect(Collectors.toList());

        List<MemberDto> memberDtos = members.stream()
                .map(member -> {
                    MemberDto dto = new MemberDto();
                    dto.setUserId(member.getUser().getId());
                    dto.setNickname(member.getUser().getNickname());
                    dto.setOwner(member.getIsOwner());
                    return dto;
                })
                .collect(Collectors.toList());

        response.setSchedules(scheduleDtos);
        response.setMembers(memberDtos);
        return response;
    }
}
