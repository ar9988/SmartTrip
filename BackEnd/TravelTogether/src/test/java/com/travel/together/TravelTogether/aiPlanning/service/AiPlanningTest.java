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
    private OdsayService odsayService;
    private DirectionsService directionsService;

    @BeforeEach
    void setUp() {
        kakaoService = new KakaoService();
        openaiService = new OpenaiService();
        odsayService = new OdsayService();
        directionsService = new DirectionsService();
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
        requestDto.setDestination(Arrays.asList("서울특별시", "경기도 과천시"));

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

        // 첫 번째 활동의 장소 이름을 가져오기
        OpenaiResponseDto.DateActivities firstDay = response.getSchedule().getDays().get(0);
        assertFalse(firstDay.getActivities().isEmpty(), "첫째 날의 활동이 비어 있으면 안 됩니다.");

        // 첫 번째 활동 정보 저장
        OpenaiResponseDto.Activity activity1 = firstDay.getActivities().get(0);
        String keyword1 = activity1.getName(); // 첫 번째 활동의 장소명

        // 두 번째 활동 정보 저장
        OpenaiResponseDto.Activity activity2 = firstDay.getActivities().get(1);
        String keyword2 = activity2.getName(); // 첫 번째 활동의 장소명

        //API 테스트 (추천된 장소 검색)
        KakaoDto place1 = testKakaoPlaceSearch(keyword1);
        KakaoDto place2 = testKakaoPlaceSearch(keyword2);
        System.out.println("장소1:" + keyword1);
        System.out.println("경도: " + place1.getLongitude());
        System.out.println("위도: " + place1.getLatitude());
        System.out.println("장소2:" + keyword2);
        System.out.println("경도: " + place2.getLongitude());
        System.out.println("위도: " + place2.getLatitude());

        OdsayResponseDto odsayResponse = testOdsay(place1.getLongitude(), place1.getLatitude(), place2.getLongitude(), place2.getLatitude());
        int totaltimeOdsay = 0;
        if (odsayResponse != null) {
            totaltimeOdsay = odsayResponse.getTotalTime(); // totalTime 추출하여 할당
        }
        System.out.println("대중교통 시간:" + totaltimeOdsay);

        // testDirections 메서드 호출 후, totalTime을 추출하여 할당
        DirectionsResponseDto directionsResponse = testDirections(place1.getLongitude(), place1.getLatitude(), place2.getLongitude(), place2.getLatitude());

        int totaltimeDirections = 0;
        if (directionsResponse != null) {
            totaltimeDirections = directionsResponse.getTotalTime(); // totalTime 추출하여 할당
        }

        System.out.println("자가용 소요 시간: " + totaltimeDirections + "분");

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

    // 3. Odsay 테스트
    OdsayResponseDto testOdsay(Double startLongitude, Double startLatitude, Double endLongitude, Double endLatitude){
        try {
            // Given: OpenAI에서 받은 장소 이름 (테스트용 데이터)
            OdsayRequestDto odsayRequestDto = new OdsayRequestDto(startLongitude, startLatitude, endLongitude, endLatitude);

            // When: Kakao API 호출하여 해당 장소 검색
            OdsayResponseDto odsayResponse = odsayService.getPublicTransportPath(odsayRequestDto);

            // Then: 응답 검증
            assertNotNull(odsayResponse, "odsay API 응답이 null이면 안 됩니다.");

            // 검색된 장소 목록 확인
            return odsayResponse;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 4. Directions 테스트
    DirectionsResponseDto testDirections(Double startLongitude, Double startLatitude, Double endLongitude, Double endLatitude){
        try {
            // Given: OpenAI에서 받은 장소 이름 (테스트용 데이터)
            DirectionsRequestDto directionsRequestDto = new DirectionsRequestDto(startLongitude, startLatitude, endLongitude, endLatitude);

            // When: Directions API 호출하여 해당 경로의 운전 정보 검색
            DirectionsResponseDto directionsResponse = directionsService.getDrivingDirections(directionsRequestDto);

            // Then: 응답 검증
            assertNotNull(directionsResponse, "Directions API 응답이 null이면 안 됩니다.");

            // 경로의 총 소요 시간과 경로 정보 반환
            return directionsResponse; // DirectionsResponseDto 객체 반환 (총 소요 시간과 경로 정보 포함)
        } catch (Exception e) {
            e.printStackTrace();
            return null; // 오류가 발생하면 null 반환
        }
    }
}
