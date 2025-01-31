package com.travel.together.TravelTogether.aiPlanning.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.json.JSONObject;

@Getter
@NoArgsConstructor
public class FestivalResponseDto {
    private JSONObject festivals; // json 형태의 축제 데이터

    // 생성자
    public FestivalResponseDto(JSONObject festivals) {
        this.festivals = festivals;
    }
}
