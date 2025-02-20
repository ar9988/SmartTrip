package com.travel.together.TravelTogether.tripwebsocket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
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

    public ScheduleDTO(Integer id, String placeName, long timeStamp, Integer positionPath, Integer duration, Double lat, Double lng, Integer type) {
        this.id = id;
        this.placeName = placeName;
        this.timeStamp = timeStamp;
        this.positionPath = positionPath;
        this.duration = duration;
        this.lat = lat;
        this.lng = lng;
        this.type = type;
    }

    public ScheduleDTO() {

    }
}
