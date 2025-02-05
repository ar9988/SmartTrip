package com.travel.together.TravelTogether.aiPlanning.service;

import com.travel.together.TravelTogether.aiPlanning.dto.OpenaiRequestDto;
import com.travel.together.TravelTogether.aiPlanning.dto.OpenaiRequestDto.Preferences;
import com.travel.together.TravelTogether.aiPlanning.dto.OpenaiResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class OpenaiServiceTest {

    private OpenaiService openaiService;

    @BeforeEach
    void setUp() {
        openaiService = new OpenaiService(); // 실제 서비스 객체 생성
    }

    @Test
    void testCallOpenaiApi() {
        // Given: 요청 DTO 생성
        OpenaiRequestDto requestDto = new OpenaiRequestDto();
        requestDto.setUserId("moyeo1234");
        requestDto.setStartDate("2025-02-05");
        requestDto.setStartTime("08:00");
        requestDto.setEndDate("2025-02-06");
        requestDto.setEndTime("22:00");
        requestDto.setDestination(Arrays.asList("서울특별시"));

        Preferences preferences = new Preferences();
        preferences.setPlaces(Arrays.asList("축제", "관광지"));
        preferences.setTheme(Arrays.asList("mountain", "park", "museum"));
        requestDto.setPreferences(preferences);

        // When: 실제 API 호출
        OpenaiResponseDto response = openaiService.callOpenaiApi(requestDto);

        // Then: 응답 검증
        assertNotNull(response, "응답이 null이면 안됩니다.");
        assertNotNull(response.getResponseMessage(), "응답 메시지가 null이면 안됩니다.");
        assertFalse(response.getResponseMessage().isEmpty(), "응답 메시지가 비어 있으면 안됩니다.");

        // 응답 JSON이 올바른지 확인 (예제 검증)
        System.out.println("응답 메시지: " + response.getResponseMessage());
    }
}
