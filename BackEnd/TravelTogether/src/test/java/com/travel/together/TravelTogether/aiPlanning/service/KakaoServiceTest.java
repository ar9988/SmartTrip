package com.travel.together.TravelTogether.aiPlanning.service;

import com.travel.together.TravelTogether.aiPlanning.dto.KakaoRequestDto;
import com.travel.together.TravelTogether.aiPlanning.dto.KakaoResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;

class KakaoServiceTest {
    private KakaoService kakaoApiService;

    @BeforeEach
    public void setUp() {
        kakaoApiService = new KakaoService();
    }

    @Test
    public void testSearchByKeyword_Success() throws UnsupportedEncodingException {
        // Arrange
        KakaoRequestDto requestDto = new KakaoRequestDto("서울");

        // Act
        KakaoResponseDto response = kakaoApiService.searchByKeyword(requestDto);

        // Assert
        System.out.println("장소 = " + response.getPlaceName());
        System.out.println("경도 = " + response.getLongitude());
        System.out.println("위도 = " + response.getLatitude());
    }
}