//package com.travel.together.TravelTogether.aiPlanning.service;
//
//import com.travel.together.TravelTogether.aiPlanning.dto.OpenaiRequestDto;
//import com.travel.together.TravelTogether.aiPlanning.dto.OpenaiRequestDto.Preferences;
//import com.travel.together.TravelTogether.aiPlanning.dto.OpenaiResponseDto;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class OpenaiServiceTest {
//
//    private OpenaiService openaiService;
//
//    @BeforeEach
//    void setUp() {
//        openaiService = new OpenaiService(); // 실제 서비스 객체 생성
//    }
//
//    @Test
//    void testCallOpenaiApi() {
//        // Given: 요청 DTO 생성
//        OpenaiRequestDto requestDto = new OpenaiRequestDto();
//        requestDto.setUserId("moyeo1234");
//        requestDto.setStartDate("2025-02-05");
//        requestDto.setStartTime("10:00");
//        requestDto.setEndDate("2025-02-06");
//        requestDto.setEndTime("22:00");
//        requestDto.setDestination(Arrays.asList("서울특별시", "경기도 과천시"));
//
//        Preferences preferences = new Preferences();
//        preferences.setPlaces(Arrays.asList("축제", "관광지"));
//        preferences.setTheme(Arrays.asList("mountain", "park", "museum"));
//        requestDto.setPreferences(preferences);
//
//        // When: 실제 API 호출
//        OpenaiResponseDto response = openaiService.callOpenaiApi(requestDto);
//
//        // Then: 응답 검증
//        assertNotNull(response, "응답이 null이면 안됩니다.");
//
//        // 날짜별 일정 조회
//        List<OpenaiResponseDto.DateActivities> days = response.getSchedule().getDays(); // days 리스트(첫째날, 둘째날... 조회)
//        for (int i = 0; i < days.size(); i++) {
//            OpenaiResponseDto.DateActivities day = days.get(i);
//            // 해당 날짜의 일정 조회
//            List<OpenaiResponseDto.Activity> activities = day.getActivities(); // activities 리스트
//            for (int j = 0; j < activities.size() ; j++) {
//                OpenaiResponseDto.Activity activity = activities.get(j);
//                System.out.println("name : " + activity.getName()); // 장소명
//                System.out.println("duration : " + activity.getDuration()); // 소요시간
//                System.out.println("type: " + activity.getType()); // 타입(관광지:1, 식사:2)
//                System.out.println("positionPath: " + activity.getPositionPath()); // 첫날 10000~19999
//            }
//            System.out.println("-----------------------------------------------------");
//        }
//
//
//
//    }
//}
