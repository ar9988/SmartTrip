package com.travel.together.TravelTogether.aiPlanning.service;

import com.travel.together.TravelTogether.aiPlanning.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AiPlanningTest {
    private OpenaiService openaiService;
    private KakaoService kakaoService;

    @BeforeEach
    void setUp() {
        kakaoService = new KakaoService();
        openaiService = new OpenaiService();
    }

    @Test // 1. Openai로 자료 받아오기
    void testAiplanning() {
        // Given: OpenaiRequestDto 생성
        OpenaiRequestDto requestDto = new OpenaiRequestDto();
        requestDto.setUserId("moyeo1234");
        requestDto.setStartDate("2025-02-05");
        requestDto.setStartTime("10:00");
        requestDto.setEndDate("2025-02-06");
        requestDto.setEndTime("22:00");
        requestDto.setDestination(Arrays.asList("서울특별시", "경기도 수원시"));

        OpenaiRequestDto.Preferences preferences = new OpenaiRequestDto.Preferences();
        preferences.setPlaces(Arrays.asList("축제", "관광지"));
        preferences.setTheme(Arrays.asList("mountain", "park", "museum"));
        requestDto.setPreferences(preferences);

        // When: OpenAI API 호출하여 추천 장소 조회
        OpenaiResponseDto response = openaiService.callOpenaiApi(requestDto);

        // Then: 응답 검증
        assertNotNull(response, "OpenAI 응답이 null이면 안 됩니다.");
        assertNotNull(response.getSchedule(), "일정 데이터가 null이면 안 됩니다.");
        assertFalse(response.getSchedule().getDays().isEmpty(), "날짜별 일정이 비어 있으면 안 됩니다.");

        // Iterate through all the days and activities
        for (int i = 0; i < response.getSchedule().getDays().size(); i++) {
            OpenaiResponseDto.DateActivities dayActivities = response.getSchedule().getDays().get(i);
            assertFalse(dayActivities.getActivities().isEmpty(), "활동이 비어 있으면 안 됩니다.");

            for (int j = 0; j < dayActivities.getActivities().size(); j++) {
                OpenaiResponseDto.Activity activity = dayActivities.getActivities().get(j);
                String keyword = activity.getName(); // 활동 이름

                // API 테스트 (추천된 장소 검색)
                System.out.println("검색 장소: " + keyword);
                KakaoDto place = testKakaoPlaceSearch(keyword);
                System.out.println("카카오 장소: " + place.getPlaceName());
                System.out.println("경도: " + place.getLongitude());
                System.out.println("위도: " + place.getLatitude());
                System.out.println("시간: " + activity.getDuration());
                System.out.println("------------------------");
            }
            System.out.println("===============================================================");
        }

    }

    // 2. Kakao API 테스트
    KakaoDto testKakaoPlaceSearch(String keyword) {
        try {
            // Given: OpenAI에서 받은 장소 이름 (테스트용 데이터)
            KakaoRequestDto kakaoRequestDto = new KakaoRequestDto(keyword);

            // When: Kakao API 호출하여 해당 장소 검색
            KakaoResponseDto kakaoResponse = kakaoService.searchByKeyword(kakaoRequestDto);

            // Then: 응답 검증
            assertNotNull(kakaoResponse, "Kakao API 응답이 null이면 안 됩니다.");
            assertFalse(kakaoResponse.getPlaces().isEmpty(), "Kakao API 검색 결과가 비어 있으면 안 됩니다.");

            // 검색된 장소 목록 확인
            List<KakaoDto> places = kakaoResponse.getPlaces();
            KakaoDto place = places.get(0); // 첫 번째 장소를 사용

            // KakaoDto 객체 반환
            return place;
        } catch (Exception e) {
            e.printStackTrace();
            return null;  // 에러 발생 시 null 반환
        }
    }
}
