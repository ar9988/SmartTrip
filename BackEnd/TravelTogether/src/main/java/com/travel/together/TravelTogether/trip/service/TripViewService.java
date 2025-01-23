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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    private final TripRepository tripRepository;
    private final ScheduleRepository scheduleRepository;
    private final TripMemberRepository tripMemberRepository;


    // 전체 여행 조회
    public TripResponse getAllTrip(Integer userId) {
        List<Trip> trips = tripRepository.findAll();
        Map<Integer, Long> memberCounts = tripMemberRepository.countByTripIdIn(
                trips.stream().map(Trip::getId).collect(Collectors.toList())
        );

        return TripResponse.from(
                trips,
                trip -> memberCounts.getOrDefault(trip.getId(), 0L).intValue(),
                trip -> "",
                trip -> LocalDateTime.now().isBefore(trip.getEndDate())
        );
    }
    // MemberCount
    public TripResponse getTrips(Integer userId) {
        List<Trip> trips = tripRepository.findAll();

        // 각 Trip에 대한 멤버 수를 한 번에 조회
        Map<Integer, Long> memberCounts = tripMemberRepository.countByTripIdIn(
                trips.stream().map(Trip::getId).collect(Collectors.toList())
        );

        return TripResponse.from(
                trips,
                trip -> memberCounts.getOrDefault(trip.getId(), 0L).intValue(),
                trip -> "", // 썸네일 로직
                trip -> LocalDateTime.now().isBefore(trip.getEndDate())
        );
    }

    // 상세 조회
    public TripDetailResponse getTripDetail(Integer userId, Integer tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripNotFoundException("여행을 찾을 수 없습니다."));

        if (!tripMemberRepository.existsByTripIdAndUserId(tripId, userId)) {
            throw new UnauthorizedException("여행 참여자가 아닙니다.");
        }

        List<Schedule> schedules = scheduleRepository.findByTripIdOrderByDayAscOrderNumAsc(tripId);
        List<TripMember> members = tripMemberRepository.findByTripId(tripId);

        return TripDetailResponse.from(trip, schedules, members);
    }


}
