//package com.travel.together.TravelTogether.aiPlanning.service;
//
//import com.travel.together.TravelTogether.aiPlanning.dto.*;
//import com.travel.together.TravelTogether.auth.entity.User;
//import com.travel.together.TravelTogether.auth.repository.UserRepository;
//import com.travel.together.TravelTogether.trip.entity.Day;
//import com.travel.together.TravelTogether.trip.entity.Schedule;
//import com.travel.together.TravelTogether.trip.entity.Trip;
//import com.travel.together.TravelTogether.trip.repository.DayRepository;
//import com.travel.together.TravelTogether.trip.repository.ScheduleRepository;
//import com.travel.together.TravelTogether.trip.repository.TripRepository;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.time.LocalDateTime;
//import java.util.Arrays;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest
//class AiplanningServiceTest {
//
//    @Autowired
//    private AiplanningService aiplanningService;
//
//    @Autowired
//    private ScheduleRepository scheduleRepository;
//
//    @Autowired
//    private TripRepository tripRepository;
//
//    @Autowired
//    private DayRepository dayRepository;
//
//    @Autowired
//    private KakaoService kakaoService;
//
//    @Autowired
//    private OpenaiService openaiService;
//
//    @Test
//    void testSavePlanningData() {
//        // 1. 기존에 저장된 Trip 객체 가져오기 (예: ID가 1인 Trip)
//        Trip existingTrip = tripRepository.findById(1)
//                .orElseThrow(() -> new RuntimeException("Trip not found"));
//
//        // 2. 기존에 저장된 Day 객체 가져오기 (예: Trip의 ID에 맞는 Day)
//        Day existingDay = dayRepository.findById(1)
//                .orElseThrow(() -> new RuntimeException("Day not found"));
//
//        // 3. OpenAI API 호출하여 추천 장소 조회
//        OpenaiRequestDto requestDto = new OpenaiRequestDto();
//        requestDto.setUserId("moyeo1234");
//        requestDto.setStartDate("2025-02-05");
//        requestDto.setStartTime("10:00");
//        requestDto.setEndDate("2025-02-06");
//        requestDto.setEndTime("22:00");
//        requestDto.setDestination(Arrays.asList("서울특별시", "경기도 과천시"));
//
//        OpenaiRequestDto.Preferences preferences = new OpenaiRequestDto.Preferences();
//        preferences.setPlaces(Arrays.asList("축제", "관광지"));
//        preferences.setTheme(Arrays.asList("mountain", "park", "museum"));
//        requestDto.setPreferences(preferences);
//
//        OpenaiResponseDto response = openaiService.callOpenaiApi(requestDto);
//        OpenaiResponseDto.DaySchedule schedule = response.getSchedule();
//
//        // 4. 장소 정보 처리
//        for (OpenaiResponseDto.DateActivities dateActivities : schedule.getDays()) {
//            for (OpenaiResponseDto.Activity activity : dateActivities.getActivities()) {
//                KakaoRequestDto kakaoRequestDto = new KakaoRequestDto(activity.getName());
//                KakaoResponseDto kakaoResponse = kakaoService.searchByKeyword(kakaoRequestDto);
//
//                // 장소 정보가 없으면 오류가 나지 않도록 처리
//                if (kakaoResponse == null || kakaoResponse.getPlaces().isEmpty()) {
//                    System.out.println("Place not found: " + activity.getName());
//                    continue;
//                }
//
//                // "식사"인 경우 lat과 lng를 null로 설정
//                Double lat = "식사".equals(activity.getName()) ? null : kakaoResponse.getPlaces().get(0).getLatitude();
//                Double lng = "식사".equals(activity.getName()) ? null : kakaoResponse.getPlaces().get(0).getLongitude();
//
//
//                // Aiplanning 객체 생성 및 저장
//                Schedule planningData = Schedule.builder()
//                        .placeName(activity.getName())
//                        .lat(lat)  // lat 값 설정
//                        .lng(lng)  // lng 값 설정
//                        .type(activity.getType())
//                        .positionPath(activity.getPositionPath())
//                        .duration(activity.getDuration())
//                        .trip(existingTrip)  // 이미 존재하는 Trip 객체 설정
//                        .day(existingDay)    // 이미 존재하는 Day 객체 설정
//                        .orderNum(1)         // order_num 값 설정
//                        .build();
//
//                scheduleRepository.save(planningData);  // Schedule 저장
//            }
//        }
//
//        // 5. 저장된 데이터 확인
//        List<Schedule> savedData = scheduleRepository.findAll();
//        System.out.println("saveddata:" + savedData);
//
//        assertThat(savedData).isNotEmpty();
//    }
//}
