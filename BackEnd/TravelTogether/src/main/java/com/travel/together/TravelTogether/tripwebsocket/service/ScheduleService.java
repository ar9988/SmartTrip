package com.travel.together.TravelTogether.tripwebsocket.service;


import com.travel.together.TravelTogether.trip.entity.Day;
import com.travel.together.TravelTogether.trip.entity.Schedule;
import com.travel.together.TravelTogether.trip.entity.Trip;
import com.travel.together.TravelTogether.trip.repository.DayRepository;
import com.travel.together.TravelTogether.trip.repository.ScheduleRepository;
import com.travel.together.TravelTogether.trip.repository.TripRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final TripRepository tripRepository;
    private final DayRepository dayRepository;

    public ScheduleService(ScheduleRepository scheduleRepository, TripRepository tripRepository, DayRepository dayRepository) {
        this.scheduleRepository = scheduleRepository;
        this.tripRepository = tripRepository;
        this.dayRepository = dayRepository;
    }

    @Transactional
    public void deleteSchedule(Integer scheduleId) {
        // 삭제 전 유효성 검사 등 비즈니스 로직
        // 연관된 데이터 처리
        scheduleRepository.deleteById(scheduleId);
    }

    @Transactional
    public Schedule addSchedule(Integer tripId, Integer dayId,String placeName,Double lat, Double lng) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found: " + tripId));

        Day day = dayRepository.findById(dayId)
                .orElseThrow(() -> new IllegalArgumentException("Day not found: " + dayId));

        // dayId에 따른 position_path 계산
        Integer positionPath = (dayId * 10000) - 1;  // day1 -> 19999, day2 -> 29999

        Schedule schedule = Schedule.builder()
                .trip(trip)
                .day(day)
                .placeName(placeName)
                .lat(lat)
                .lng(lng)
                .type(1)
                .positionPath(positionPath)
                .build();

        return scheduleRepository.save(schedule);

    }

}
