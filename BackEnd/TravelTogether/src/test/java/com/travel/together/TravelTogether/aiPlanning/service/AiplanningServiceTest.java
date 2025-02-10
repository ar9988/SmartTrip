package com.travel.together.TravelTogether.aiPlanning.service;

import com.travel.together.TravelTogether.aiPlanning.dto.*;
import com.travel.together.TravelTogether.aiPlanning.entity.Aiplanning;
import com.travel.together.TravelTogether.aiPlanning.repository.AiplanningRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AiplanningServiceTest {

    @Autowired
    private AiplanningService aiplanningService;

    @Autowired
    private AiplanningRepository aiplanningRepository;

    @Autowired
    private KakaoService kakaoService;  // 실제 KakaoService 사용

    @Autowired
    private OpenaiService openaiService;

    @Test
    void testSavePlanningData() {
        // 1. OpenAI API 호출을 위한 테스트 데이터 준비
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

        // OpenAI API 호출하여 추천 장소 조회
        OpenaiResponseDto response = openaiService.callOpenaiApi(requestDto);

        // 2. OpenAI 응답에서 일정을 가져옴
        OpenaiResponseDto.DaySchedule schedule = response.getSchedule();

        // 3. 실제 KakaoService를 사용하여 장소 정보를 가져오는 부분
        for (OpenaiResponseDto.DateActivities dateActivities : schedule.getDays()) {
            for (OpenaiResponseDto.Activity activity : dateActivities.getActivities()) {
                KakaoRequestDto kakaoRequestDto = new KakaoRequestDto(activity.getName());
                KakaoResponseDto kakaoResponse = kakaoService.searchByKeyword(kakaoRequestDto);

                // 4. 장소 정보가 없으면 오류가 나기 전에 정상적으로 처리되도록 확인
                if (kakaoResponse == null || kakaoResponse.getPlaces().isEmpty()) {
                    System.out.println("Place not found: " + activity.getName());
                    continue;
                }

                KakaoDto place = kakaoResponse.getPlaces().get(0);

                // Aiplanning 객체 생성 및 저장
                Aiplanning planningData = Aiplanning.builder()
                        .placeName(activity.getName())
                        .lat(place.getLatitude() != null ? place.getLatitude() : 0.0)
                        .lng(place.getLongitude() != null ? place.getLongitude() : 0.0)
                        .type(activity.getType())
                        .positionPath(activity.getPositionPath())
                        .duration(activity.getDuration())
                        .build();

                aiplanningRepository.save(planningData);
            }
        }

        // 5. 저장된 데이터 확인
        List<Aiplanning> savedData = aiplanningRepository.findAll();
        System.out.println("saveddata:" + savedData);

        assertThat(savedData).isNotEmpty();
        assertThat(savedData.get(0).getDuration()).isEqualTo(120);
        assertThat(savedData.get(0).getLat()).isNotNull();
        assertThat(savedData.get(0).getLng()).isNotNull();
    }
}
