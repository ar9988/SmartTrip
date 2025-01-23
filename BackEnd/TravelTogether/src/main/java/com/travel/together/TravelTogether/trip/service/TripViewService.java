package com.travel.together.TravelTogether.trip.service;

import com.travel.together.TravelTogether.trip.dto.TripDetailResponse;
import com.travel.together.TravelTogether.trip.dto.TripResponse;
import com.travel.together.TravelTogether.trip.entity.Schedule;
import com.travel.together.TravelTogether.trip.entity.Trip;
import com.travel.together.TravelTogether.trip.entity.TripMember;
import com.travel.together.TravelTogether.trip.exception.TripNotFoundException;
import com.travel.together.TravelTogether.trip.exception.UnauthorizedException;
import com.travel.together.TravelTogether.trip.repository.ScheduleRepository;
import com.travel.together.TravelTogether.trip.repository.TripMemberRepository;
import com.travel.together.TravelTogether.trip.repository.TripRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
//@Transactional(readOnly = true)
//@RequiredArgsConstructor
public class TripViewService {
    public TripViewService(TripRepository tripRepository, ScheduleRepository scheduleRepository, TripMemberRepository tripMemberRepository) {
        this.tripRepository = tripRepository;
        this.scheduleRepository = scheduleRepository;
        this.tripMemberRepository = tripMemberRepository;
    }
    @Autowired
    private final TripRepository tripRepository;
    @Autowired
    private final ScheduleRepository scheduleRepository;
    @Autowired
    private final TripMemberRepository tripMemberRepository;


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
    public TripDetailResponse getTripDetail(Integer userId, Integer tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripNotFoundException("여행을 찾을 수 없습니다."));
//
//        if (!tripMemberRepository.existsByTripIdAndUserId(tripId, userId)) {
//            throw new UnauthorizedException("여행 참여자가 아닙니다.");
//        }

        List<Schedule> schedules = scheduleRepository.findByTripIdOrderByDayAscOrderNumAsc(tripId);
        List<TripMember> members = tripMemberRepository.findByTripId(tripId);

        return TripDetailResponse.from(trip, schedules, members);
    }


}
