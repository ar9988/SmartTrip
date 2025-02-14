package com.travel.together.TravelTogether.trip.service;

import com.travel.together.TravelTogether.auth.entity.User;
import com.travel.together.TravelTogether.auth.repository.UserRepository;
import com.travel.together.TravelTogether.trip.dto.*;
import com.travel.together.TravelTogether.trip.entity.*;
import com.travel.together.TravelTogether.trip.exception.TripNotFoundException;
import com.travel.together.TravelTogether.trip.exception.UnauthorizedException;
import com.travel.together.TravelTogether.trip.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class TripViewService {
    public TripViewService(TripRepository tripRepository, ScheduleRepository scheduleRepository, TripMemberRepository tripMemberRepository, DayRepository dayRepository, RouteRepository routeRepository, UserRepository userRepository) {
        this.tripRepository = tripRepository;
        this.scheduleRepository = scheduleRepository;
        this.tripMemberRepository = tripMemberRepository;
        this.dayRepository = dayRepository;
        this.routeRepository = routeRepository;
        this.userRepository = userRepository;
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



    // 전체 여행 조회
    @Transactional
    public TripResponse getAllTrip(Integer userId) {
        List<Trip> trips = tripRepository.findTripsByUserId(userId);

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
                .build();
        tripMemberRepository.save(tripMember);

        // DTO 변환 후 반환
        return TripCreateDto.from(savedTrip);
    }




    // 현재시간 이후의 가장 최근 여행 1개 조회해서보내주기

    @Transactional
    public TripResponse findUpcomingTrip() {
        LocalDateTime now = LocalDateTime.now();

        Optional<Trip> tripOptional = tripRepository.findFirstByStartDateGreaterThanOrderByStartDateAsc(now);

        if (tripOptional.isEmpty()) {
            return null;
        }

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


