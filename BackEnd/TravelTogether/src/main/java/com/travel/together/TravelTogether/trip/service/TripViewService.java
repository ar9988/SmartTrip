package com.travel.together.TravelTogether.trip.service;

import com.travel.together.TravelTogether.trip.repository.ScheduleRepository;
import com.travel.together.TravelTogether.trip.repository.TripMemberRepository;
import com.travel.together.TravelTogether.trip.repository.TripRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
//@Transactional(readOnly = true)
//@RequiredArgsConstructor
public class TripViewService {
//    public TripViewService(TripRepository tripRepository, ScheduleRepository scheduleRepository, TripMemberRepository tripMemberRepository) {
//        this.tripRepository = tripRepository;
//        this.scheduleRepository = scheduleRepository;
//        this.tripMemberRepository = tripMemberRepository;
//    }
//    private final TripRepository tripRepository;
//    private final ScheduleRepository scheduleRepository;
//    private final TripMemberRepository tripMemberRepository;
//
//
//    public TripScheduleResponse getAllSchedules(Long tripId) {
//        Trip trip = tripRepository.findById(tripId)
//                .orElseThrow(() -> new TripNotFoundException("여행을 찾을 수 없습니다."));
//
//        List<Schedule> schedules = scheduleRepository.findByTripIdOrderByDayAscOrderAsc(tripId);
//
//        return TripScheduleResponse.from(trip, schedules);
//    }
//
//    public TripDetailResponse getTripDetail(Long userId, Long tripId) {
//        Trip trip = tripRepository.findById(tripId)
//                .orElseThrow(() -> new TripNotFoundException("여행을 찾을 수 없습니다."));
//
//        if (!tripMemberRepository.existsByTripIdAndUserId(tripId, userId)) {
//            throw new UnauthorizedException("여행 참여자가 아닙니다.");
//        }
//
//        List<Schedule> schedules = scheduleRepository.findByTripIdOrderByDayAscOrderAsc(tripId);
//        List<TripMember> members = tripMemberRepository.findByTripId(tripId);
//
//        return TripDetailResponse.from(trip, schedules, members);
//    }
}
