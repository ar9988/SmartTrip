package com.travel.together.TravelTogether.trip.service;

import com.travel.together.TravelTogether.album.repository.PhotoAlbumRepository;
import com.travel.together.TravelTogether.auth.entity.User;
import com.travel.together.TravelTogether.auth.repository.UserRepository;
import com.travel.together.TravelTogether.trip.dto.*;
import com.travel.together.TravelTogether.trip.entity.*;
import com.travel.together.TravelTogether.trip.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class TripViewService {
    public TripViewService(TripRepository tripRepository, ScheduleRepository scheduleRepository, TripMemberRepository tripMemberRepository, DayRepository dayRepository, RouteRepository routeRepository, UserRepository userRepository, PhotoAlbumRepository photoAlbumRepository) {
        this.tripRepository = tripRepository;
        this.scheduleRepository = scheduleRepository;
        this.tripMemberRepository = tripMemberRepository;
        this.dayRepository = dayRepository;
        this.routeRepository = routeRepository;
        this.userRepository = userRepository;
        this.photoAlbumRepository = photoAlbumRepository;
    }

    @Autowired
    private final TripRepository tripRepository;
    @Autowired
    private final ScheduleRepository scheduleRepository;
    @Autowired
    private final TripMemberRepository tripMemberRepository;
    @Autowired
    private final DayRepository dayRepository;
    @Autowired
    private final RouteRepository routeRepository;
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final PhotoAlbumRepository photoAlbumRepository;


    // 전체 여행 조회
    @Transactional
    public TripResponse getAllTrip(Integer userId) {
        // endDate가 현재시각 이후인 여행만 조회
        List<Trip> trips = tripRepository.findActiveTripsByUserId(userId);


        // 각 여행별 멤버 수 계산
        List<Object[]> countResults = tripMemberRepository.countMembersByTripId();
        Map<Integer, Long> memberCounts = new HashMap<>();
        for (Object[] result : countResults) {
            Integer tripId = (Integer) ((Number) result[0]).intValue();
            Long count = (Long) ((Number) result[1]).longValue();
            memberCounts.put(tripId, count);
        }

        // Response 생성
        return TripResponse.from(
                trips,
                trip -> (Integer) memberCounts.getOrDefault(trip.getId(), Long.valueOf(0L)).intValue(),
                trip -> "",
                trip -> (Boolean) LocalDateTime.now().isBefore(trip.getEndDate())
        );
    }

    // 상세 조회
    public TripDetailResponse getTripDetail(Integer userId,Integer tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new EntityNotFoundException("Trip not found"));

        // 권한 체크: creator이거나 tripMember인 경우만 조회 가능
        if (!trip.getCreator().getId().equals(userId) &&
                !tripMemberRepository.existsByTripIdAndUserId(tripId, userId)) {
            throw new AccessDeniedException("권한이 없습니다.");
        }


        List<TripMember> members = tripMemberRepository.findByTripId(tripId);
        List<Day> days = dayRepository.findByTripId(tripId);

        List<DayDto> dayDtos = days.stream()
                .map(day -> {
                    List<Schedule> schedules = scheduleRepository.findByDayId(day.getId());
                    List<Route> routes = routeRepository.findByDayId(day.getId());
                    return new DayDto(day, schedules, routes);
                })
                .collect(Collectors.toList());

        return new TripDetailResponse(trip, members, dayDtos);
    }




    @Transactional
    public TripCreateDto createTrip(TripRequestDto requestDto) {
        // 사용자 조회
        User creator = userRepository.findById(Long.valueOf(requestDto.getUserId()))
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Trip 엔티티 생성
        Trip trip = Trip.builder()
                .creator(creator)
                .title(requestDto.getTitle())
                .startDate(requestDto.getStartDate())
                .endDate(requestDto.getEndDate())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 저장
        Trip savedTrip = tripRepository.save(trip);

        // 여행 생성자를 trip_member 테이블에도 추가
        TripMember tripMember = TripMember.builder()
                .trip(savedTrip)
                .user(creator)
                .isOwner(true)
                .build();
        tripMemberRepository.save(tripMember);

        // 연관 데이터 생성 (별도 메소드로 분리)
        createTripRelatedData(savedTrip);


        // DTO 변환 후 반환
        return TripCreateDto.from(savedTrip);

    }

    private void createTripRelatedData(Trip trip) {
        LocalDateTime currentDate = trip.getStartDate();
        int dayOrder = 1;

        while (!currentDate.isAfter(trip.getEndDate())) {
            // Day 생성
            Day day = Day.builder()
                    .trip(trip)
                    .startTime(currentDate)
                    .orderNum(dayOrder)
                    .build();

            Day savedDay = dayRepository.save(day);

            // Schedule 생성 (positionPath는 Day ID 기반으로 설정)
//            int positionPath = (dayOrder * 10000) + 1000;
//            Schedule schedule = Schedule.builder()
//                    .day(savedDay)
//                    .trip(trip)
//                    .placeName("시작 지점")
//                    .orderNum(1)
//                    .lat(0.0)
//                    .lng(0.0)
//                    .type(1)
//                    .positionPath(positionPath)
//                    .duration(0)
//                    .build();

//            scheduleRepository.save(schedule);

            currentDate = currentDate.plusDays(1);
            dayOrder++;
        }
    }

    @Transactional
    public void deleteTrip(Integer tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new EntityNotFoundException("Trip not found with id: " + tripId));

        // 1. 먼저 Schedule 삭제
        scheduleRepository.deleteByTripId(tripId);

        // 2. Day 삭제
        dayRepository.deleteByTripId(tripId);

        // 3. TripMember 삭제
        tripMemberRepository.deleteByTripId(tripId);

        // 4. PhotoAlbum 삭제
        if (trip.getPhotoAlbum() != null) {
            photoAlbumRepository.delete(trip.getPhotoAlbum());
        }

        // 5. 마지막으로 Trip 삭제
        tripRepository.delete(trip);
        log.info("Trip deletion completed for ID: {}", tripId);

    }

    public void updateTrip(Integer tripId, TripUpdateRequest request) {


        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new EntityNotFoundException("Trip not found"));
        log.info("Trip found: {}", trip);

        trip.updateTrip(
                request.getTitle(),
                request.getStartDate(),
                request.getEndDate()
        );

        tripRepository.save(trip);
    }


    // 현재시간 이후의 가장 최근 여행 1개 조회해서보내주기

    @Transactional
    public TripResponse findUpcomingTrip(Integer userId) {
        LocalDateTime now = LocalDateTime.now();

        log.info("1");
        Optional<Trip> tripOptional = tripRepository.findUpcomingTripByUserId(userId);

        log.info("2");

        if (tripOptional.isEmpty()) {
            return null;
        }

        log.info("3");
        Trip trip = tripOptional.get();
        Integer memberCount = tripMemberRepository.countByTripId(trip.getId());

        return TripResponse.from(
                List.of(trip),
                t -> memberCount,  // 단일 건이므로 미리 조회한 memberCount 사용
                t -> "",  // 기본 thumbnail 값 ""
                t -> false  // 기본 status 값 false
        );
    }

    private Integer getMemberCount(Trip trip) {
        return tripMemberRepository.countByTripId(trip.getId());
    }



}


