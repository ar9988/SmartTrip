package com.travel.together.TravelTogether.tripwebsocket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@AllArgsConstructor
@Getter
@Setter
public class ScheduleDTO {
    @JsonProperty("scheduleId")  // JSON 응답에서 'scheduleId'로 나가도록
    private Integer id;          // DB의 실제 schedule id 사용
    private String placeName;
    private long timeStamp;      // 현재시간 timestamp
    private Integer positionPath;
    private Integer duration;
    private Double lat;
    private Double lng;
    private Integer type;

    public ScheduleDTO(Integer id, Integer duration, String placeName, Integer positionPath, Integer tripId) {
    }
}
