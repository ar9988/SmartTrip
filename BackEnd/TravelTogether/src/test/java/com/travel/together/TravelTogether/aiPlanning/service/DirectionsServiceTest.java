package com.travel.together.TravelTogether.aiPlanning.service;

import com.travel.together.TravelTogether.aiPlanning.dto.DirectionsRequestDto;
import com.travel.together.TravelTogether.aiPlanning.dto.DirectionsResponseDto;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import org.apache.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DirectionsServiceTest {

    @Test
    void testGetDrivingDirections() throws Exception {
        // Given: DirectionsRequestDto 객체 준비
        DirectionsRequestDto requestDto = new DirectionsRequestDto(127.10023101886318, 37.51331105877401, 127.06302321147605, 37.508822740225305);

        // HttpClient를 Mock 객체로 생성
        HttpClient mockHttpClient = Mockito.mock(HttpClient.class);
        DirectionsService directionsService = new DirectionsService();

        // HttpClient의 execute 메서드가 호출될 때, 가짜 HttpResponse를 반환하도록 설정
        HttpResponse mockResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(mockHttpClient.execute(Mockito.any(HttpGet.class))).thenReturn(mockResponse);

        // When: 실제 서비스 호출 (모의된 HttpClient를 사용)
        DirectionsResponseDto responseDto = directionsService.getDrivingDirections(requestDto);

        // Then: 응답에서 totalTime이 설정되어 있는지 확인
        System.out.println(responseDto.getTotalTime());
    }
}
