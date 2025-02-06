package com.travel.together.TravelTogether.aiPlanning.service;

import com.travel.together.TravelTogether.aiPlanning.dto.FestivalDto;
import com.travel.together.TravelTogether.aiPlanning.dto.FestivalRequestDto;
import com.travel.together.TravelTogether.aiPlanning.dto.FestivalResponseDto;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FestivalServiceTest {

    @Test
    void testGetFestivals() throws IOException {
        // Given: FestivalService 인스턴스 생성
        FestivalService festivalService = new FestivalService();

        // 요청 데이터 설정 (실제 API 호출)
        FestivalRequestDto requestDto = new FestivalRequestDto("20250131", "20250201", "1");

        // When: 서비스 호출
        FestivalResponseDto responseDto = festivalService.getFestivals(requestDto);

        // Then: 응답 검증
        assertNotNull(responseDto, "Response should not be null.");
        assertNotNull(responseDto.getFestivals(), "Festival list should not be null.");

        // 축제 리스트가 비어있지 않은지 확인
        List<FestivalDto> festivals = responseDto.getFestivals();
        assertFalse(festivals.isEmpty(), "Festival list should not be empty.");

        // 결과 출력
//        System.out.println("Retrieved Festivals: " + festivals);

        // 전체 축제 확인
        for (FestivalDto festival : festivals) {
            System.out.println("Title: " + festival.getTitle());
            System.out.println("Address: " + festival.getAddress());
            System.out.println("Start Date: " + festival.getEventStartDate());
            System.out.println("End Date: " + festival.getEventEndDate());
            System.out.println("Image URL: " + festival.getImageurl());
            System.out.println("contentid: " + festival.getContentid());
            System.out.println("================================");
        }
    }
}
