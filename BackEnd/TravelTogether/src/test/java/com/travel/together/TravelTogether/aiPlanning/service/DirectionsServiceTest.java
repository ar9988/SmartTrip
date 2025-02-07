package com.travel.together.TravelTogether.aiPlanning.service;

import com.travel.together.TravelTogether.aiPlanning.dto.DirectionsRequestDto;
import com.travel.together.TravelTogether.aiPlanning.dto.DirectionsResponseDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DirectionsServiceTest {

    @Test
    void testGetDrivingDirections() throws Exception {
        // Given: 출발지 & 도착지 설정
        DirectionsRequestDto requestDto = new DirectionsRequestDto(
                126.970606917394,
                37.5546788388674,
                129.04141918283216,
                35.11510918247538
        );

        // 실제 DirectionsService 인스턴스 생성
        DirectionsService directionsService = new DirectionsService();

        // When: 실제 서비스 호출
        DirectionsResponseDto responseDto = directionsService.getDrivingDirections(requestDto);

        // Then: 응답 값 검증
        assertNotNull(responseDto, "응답 객체가 null이면 안 됩니다.");
        assertNotNull(responseDto.getTotalTime(), "총 소요 시간이 설정되어 있어야 합니다.");
        assertTrue(responseDto.getTotalTime() > 0, "소요 시간은 0보다 커야 합니다.");
        assertNotNull(responseDto.getDirectionPath(), "DirectionPath가 null이면 안 됩니다.");
        assertNotNull(responseDto.getDirectionPath().getPath(), "경로 리스트가 null이면 안 됩니다.");
        assertFalse(responseDto.getDirectionPath().getPath().isEmpty(), "경로 리스트는 비어 있으면 안 됩니다.");

        // 출력
        System.out.println("총 소요 시간: " + responseDto.getTotalTime() + "분");
        for (int i = 0; i < responseDto.getDirectionPath().getPath().size(); i++) {
            DirectionsResponseDto.PathPoint point = responseDto.getDirectionPath().getPath().get(i);
            System.out.println("[" + i + "] 경도: " + point.getLongitude() + ", 위도: " + point.getLatitude());
        }
    }
}