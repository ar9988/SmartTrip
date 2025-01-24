package com.travel.together.TravelTogether.trip.service;

import com.travel.together.TravelTogether.trip.dto.DayDto;
import com.travel.together.TravelTogether.trip.dto.TripDetailResponse;
import com.travel.together.TravelTogether.trip.dto.TripResponse;
import com.travel.together.TravelTogether.trip.entity.*;
import com.travel.together.TravelTogether.trip.exception.TripNotFoundException;
import com.travel.together.TravelTogether.trip.exception.UnauthorizedException;
import com.travel.together.TravelTogether.trip.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;

import org.springframework.security.access.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
//@Transactional(readOnly = true)
//@RequiredArgsConstructor
public class TripViewService {
    public TripViewService(TripRepository tripRepository, ScheduleRepository scheduleRepository, TripMemberRepository tripMemberRepository, DayRepository dayRepository, RouteRepository routeRepository) {
        this.tripRepository = tripRepository;
        this.scheduleRepository = scheduleRepository;
        this.tripMemberRepository = tripMemberRepository;
        this.dayRepository = dayRepository;
        this.routeRepository = routeRepository;
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



    // 전체 여행 조회
    public TripResponse getAllTrip(Integer userId) {
        List<Trip> trips = tripRepository.findTripsByUserId(userId);

        // 각 여행별 멤버 수 계산
        List<Object[]> countResults = tripMemberRepository.countMembersByTripId();
        Map<Integer, Long> memberCounts = new HashMap<>();
        for (Object[] result : countResults) {
            Integer tripId = ((Number) result[0]).intValue();
            Long count = ((Number) result[1]).longValue();
            memberCounts.put(tripId, count);
        }

        // Response 생성
        return TripResponse.from(
                trips,
                trip -> memberCounts.getOrDefault(trip.getId(), 0L).intValue(),
                trip -> "",
                trip -> LocalDateTime.now().isBefore(trip.getEndDate())
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

}
