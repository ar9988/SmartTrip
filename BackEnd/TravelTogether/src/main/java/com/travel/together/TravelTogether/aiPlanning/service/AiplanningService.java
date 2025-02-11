package com.travel.together.TravelTogether.aiPlanning.service;

import com.travel.together.TravelTogether.aiPlanning.dto.*;
import com.travel.together.TravelTogether.album.entity.PhotoAlbum;
import com.travel.together.TravelTogether.album.repository.PhotoAlbumRepository;
import com.travel.together.TravelTogether.auth.entity.User;
import com.travel.together.TravelTogether.auth.repository.UserRepository;
import com.travel.together.TravelTogether.trip.entity.Day;
import com.travel.together.TravelTogether.trip.entity.Schedule;
import com.travel.together.TravelTogether.trip.entity.Trip;
import com.travel.together.TravelTogether.trip.entity.TripMember;
import com.travel.together.TravelTogether.trip.repository.DayRepository;
import com.travel.together.TravelTogether.trip.repository.ScheduleRepository;
import com.travel.together.TravelTogether.trip.repository.TripMemberRepository;
import com.travel.together.TravelTogether.trip.repository.TripRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiplanningService {
    private final ScheduleRepository scheduleRepository;
    private final KakaoService kakaoService;
    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;
    private final DayRepository dayRepository;
    private final UserRepository userRepository;
    private final PhotoAlbumRepository photoAlbumRepository;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Transactional
    public void savePlanningData(OpenaiResponseDto responseDto) {
        if (responseDto == null || responseDto.getSchedule() == null) {
            System.out.println("ResponseDto or schedule is null. Skipping processing.");
            return;
        }

        // 현재 사용자 정보 가져오기
        User user = getCurrentUser(); // 현재 사용자 정보 가져오기

        // Trip 생성 및 저장
        Trip trip = createTrip(responseDto, user);
        tripRepository.save(trip);

        // 앨범 생성
        PhotoAlbum album = new PhotoAlbum();
        album.setTrip(trip);
        album.setImageUrl("default.jpg");  // 기본 앨범 커버 이미지 설정
        photoAlbumRepository.save(album);

        // TripMember 생성 및 저장
        TripMember tripMember = new TripMember();
        tripMember.setTrip(trip);
        tripMember.setUser(user);
        tripMember.setIsOwner(true);
        tripMemberRepository.save(tripMember);

        List<OpenaiResponseDto.DateActivities> days = responseDto.getSchedule().getDays();
        if (days == null || days.isEmpty()) {
            System.out.println("No days found in schedule. Skipping processing.");
            return;
        }

        try {
            for (OpenaiResponseDto.DateActivities dayDto : days) {
                LocalDateTime startTime = parseDate(dayDto.getDate());
                Day day = new Day();
                day.setTrip(trip); // Trip 객체 설정
                day.setStartTime(startTime);
                day.setOrderNum(1);

                dayRepository.save(day);

                List<OpenaiResponseDto.Activity> activities = dayDto.getActivities();
                if (activities == null || activities.isEmpty()) {
                    System.out.println("No activities found for date: " + dayDto.getDate());
                    continue;
                }

                for (OpenaiResponseDto.Activity activity : activities) {
                    processActivity(activity, day); // Activity와 Day 객체를 함께 전달
                }
            }
        } catch (Exception e) {
            System.out.println("Error saving planning data: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to save planning data", e);
        }
    }

    // Trip 테이블 DB 입력 코드
    private Trip createTrip(OpenaiResponseDto responseDto, User user) {
        LocalDateTime startDate = parseDate(responseDto.getStartDate());
        LocalDateTime endDate = parseDate(responseDto.getEndDate());

        return Trip.builder()
                .creator(user)
                .title(responseDto.getTitle())
                .startDate(startDate)
                .endDate(endDate)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // YYYYMMDD 형식으로 출력하기 위한 parsing 코드
    private LocalDateTime parseDate(String dateString) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            LocalDate date = LocalDate.parse(dateString, formatter);
            System.out.println(date.atStartOfDay());
            return date.atStartOfDay(); // LocalDateTime 변환
        } catch (Exception e) {
            System.out.println("Invalid date format: " + dateString);
            throw new IllegalArgumentException("Invalid date format: " + dateString, e);
        }
    }

    // Response 데이터를 카카오에 조회, Schedule 테이블에 저장.
    private void processActivity(OpenaiResponseDto.Activity activity, Day day) {
        try {
            KakaoRequestDto kakaoRequestDto = new KakaoRequestDto(activity.getName());
            KakaoResponseDto kakaoResponse = kakaoService.searchByKeyword(kakaoRequestDto);

            if (kakaoResponse == null || kakaoResponse.getPlaces().isEmpty()) {
                System.out.println("Place not found: " + activity.getName());
                return;
            }

            KakaoDto place = kakaoResponse.getPlaces().get(0);

            if (place.getPlaceName().equals("식사")) {
                Double lat = 0.0;
                Double lng = 0.0;
            } else {
                Double lat = place.getLatitude();
                Double lng = place.getLongitude();
            }

            Schedule planningData = Schedule.builder()
                    .day(day)
                    .trip(day.getTrip())
                    .placeName(activity.getName())
                    .orderNum(1)
                    .lat(place.getLatitude())
                    .lng(place.getLongitude())
                    .type(activity.getType())
                    .positionPath(activity.getPositionPath())
                    .duration(activity.getDuration())
                    .build();

            System.out.println("Saving planning data: " + planningData);
            scheduleRepository.save(planningData);
        } catch (Exception e) {
            System.out.println("Error processing activity " + activity.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 현재 사용자
    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String email = ((UserDetails) principal).getUsername();  // UserDetails에서 이메일 가져오기
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }

        throw new RuntimeException("User is not authenticated");
    }
}
