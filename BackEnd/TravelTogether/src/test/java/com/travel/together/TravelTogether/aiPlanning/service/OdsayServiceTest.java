package com.travel.together.TravelTogether.aiPlanning.service;

import com.travel.together.TravelTogether.aiPlanning.dto.OdsayRequestDto;
import com.travel.together.TravelTogether.aiPlanning.dto.OdsayResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;


class OdsayServiceTest {

    private final OdsayService odsayService = new OdsayService();

    @Test
    void testGetPublicTransportPath() throws Exception {
        // Given (실제 API 호출을 위한 요청 데이터)
        OdsayRequestDto requestDto = new OdsayRequestDto(127.10023101886318, 37.51331105877401, 127.06302321147605, 37.508822740225305);

        // When (실제 API 호출)
        OdsayResponseDto responseDto = odsayService.getPublicTransportPath(requestDto);

        // Then (결과 출력)
        System.out.println("총 소요 시간: " + responseDto.getTotalTime());
    }
}