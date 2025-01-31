package com.travel.together.TravelTogether.aiPlanning.service;

import com.travel.together.TravelTogether.aiPlanning.dto.FestivalRequestDto;
import com.travel.together.TravelTogether.aiPlanning.dto.FestivalResponseDto;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class FestivalServiceTest {

    @Test
    void testGetFestivals() throws Exception {
        // Given: FestivalService 객체 생성
        FestivalService festivalService = new FestivalService(); // 실제 객체 생성

        // When: 실제 서비스 호출 (API 요청)
        FestivalRequestDto requestDto = new FestivalRequestDto("20250131","20250201", "34");
        FestivalResponseDto responseDto = festivalService.getFestivals(requestDto);

        // Then: 응답이 null이 아니어야 함
        assertNotNull(responseDto, "Response should not be null.");

        // JSON으로 변환하여 출력
//        System.out.println(responseDto);

        // JSON 내용도 확인 (festivals에 대해 확인)
        JSONObject festivals = responseDto.getFestivals();
        assertNotNull(festivals, "Festivals should not be null.");

        // 결과 출력
        System.out.println(festivals.toString());
    }
}
