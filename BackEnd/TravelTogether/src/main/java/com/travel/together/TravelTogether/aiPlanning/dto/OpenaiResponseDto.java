package com.travel.together.TravelTogether.aiPlanning.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpenaiResponseDto {
    private String responseMessage; // 답변 메시지

    public OpenaiResponseDto(String responseMessage) {
        this.responseMessage = responseMessage;
    }
}
