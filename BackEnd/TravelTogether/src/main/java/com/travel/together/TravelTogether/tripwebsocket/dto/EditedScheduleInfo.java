package com.travel.together.TravelTogether.tripwebsocket.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EditedScheduleInfo {
    private Integer duration;
    private String placeName;

    public EditedScheduleInfo() {

    }
}