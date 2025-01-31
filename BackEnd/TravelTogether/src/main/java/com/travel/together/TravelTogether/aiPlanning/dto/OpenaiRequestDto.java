package com.travel.together.TravelTogether.aiPlanning.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpenaiRequestDto {
    private final String prompt; // 프롬프트

    // Constructor
    public OpenaiRequestDto(String prompt) {
        this.prompt = prompt;
    }
}
