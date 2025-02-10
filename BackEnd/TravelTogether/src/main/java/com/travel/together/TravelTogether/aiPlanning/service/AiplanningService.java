package com.travel.together.TravelTogether.aiPlanning.service;

import com.travel.together.TravelTogether.aiPlanning.dto.*;
import com.travel.together.TravelTogether.aiPlanning.entity.Aiplanning;
import com.travel.together.TravelTogether.aiPlanning.repository.AiplanningRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiplanningService {
    private final AiplanningRepository aiplanningRepository;
    private final KakaoService kakaoService;

    @Transactional
    public void savePlanningData(OpenaiResponseDto responseDto) {
        if (responseDto == null || responseDto.getSchedule() == null) {
            System.out.println("ResponseDto or schedule is null. Skipping processing.");
            return;
        }

        List<OpenaiResponseDto.DateActivities> days = responseDto.getSchedule().getDays();
        if (days == null || days.isEmpty()) {
            System.out.println("No days found in schedule. Skipping processing.");
            return;
        }

        try {
            for (OpenaiResponseDto.DateActivities day : days) {
                List<OpenaiResponseDto.Activity> activities = day.getActivities();
                if (activities == null || activities.isEmpty()) {
                    System.out.println("No activities found for date: " + day.getDate());
                    continue;
                }

                for (OpenaiResponseDto.Activity activity : activities) {
                    processActivity(activity);
                }
            }
        } catch (Exception e) {
            System.out.println("Error saving planning data: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to save planning data", e);
        }
    }

    private void processActivity(OpenaiResponseDto.Activity activity) {
        try {
            KakaoRequestDto kakaoRequestDto = new KakaoRequestDto(activity.getName());
            KakaoResponseDto kakaoResponse = kakaoService.searchByKeyword(kakaoRequestDto);

            if (kakaoResponse == null || kakaoResponse.getPlaces().isEmpty()) {
                System.out.println("Place not found: " + activity.getName());
                return;
            }

            KakaoDto place = kakaoResponse.getPlaces().get(0);

            Aiplanning planningData = Aiplanning.builder()
                    .placeName(activity.getName())
                    .lat(place.getLatitude() != null ? place.getLatitude() : 0.0)  // 기본값 설정
                    .lng(place.getLongitude() != null ? place.getLongitude() : 0.0)
                    .type(activity.getType())
                    .positionPath(activity.getPositionPath())
                    .duration(activity.getDuration())
                    .build();

            System.out.println("Saving planning data: " + planningData);
            aiplanningRepository.save(planningData);
        } catch (Exception e) {
            System.out.println("Error processing activity " + activity.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
