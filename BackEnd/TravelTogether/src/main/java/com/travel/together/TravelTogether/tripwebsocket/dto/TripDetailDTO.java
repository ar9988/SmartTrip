package com.travel.together.TravelTogether.tripwebsocket.dto;

import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Transactional
public class TripDetailDTO {
    private Integer id;
    private String title;
    private List<MemberDTO> members;
    private List<DayDto> dayDtos;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TripDetailDTO(Integer id, String title, List<MemberDTO> members, List<DayDto> dayDtos, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.members = members;
        this.dayDtos = dayDtos;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public TripDetailDTO(TripDetailDTO tripDetail) {
    }
}
