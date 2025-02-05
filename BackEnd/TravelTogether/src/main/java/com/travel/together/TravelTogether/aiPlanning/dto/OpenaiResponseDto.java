package com.travel.together.TravelTogether.aiPlanning.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

@Getter
@Setter
@AllArgsConstructor
public class OpenaiResponseDto {
    private String responseMessage; // 답변 메시지
}

