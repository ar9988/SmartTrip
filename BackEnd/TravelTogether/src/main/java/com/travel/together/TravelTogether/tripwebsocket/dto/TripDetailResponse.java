package com.travel.together.TravelTogether.tripwebsocket.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@Getter
@Setter
public class TripDetailResponse {
    public TripDetailResponse(Integer id) {
    }
    private Integer tripId;
    private String title;
    private List<MemberDTO> members;
    private List<DayDto> day;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
