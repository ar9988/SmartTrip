package com.travel.together.TravelTogether.tripwebsocket.event;


import com.travel.together.TravelTogether.trip.entity.Day;
import com.travel.together.TravelTogether.trip.entity.Schedule;
import com.travel.together.TravelTogether.trip.repository.DayRepository;
import com.travel.together.TravelTogether.trip.repository.ScheduleRepository;
import com.travel.together.TravelTogether.tripwebsocket.dto.RouteResponse;
import com.travel.together.TravelTogether.tripwebsocket.service.RouteService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RouteCalculatorScheduler {
    private final DayRepository dayRepository;
    private final ScheduleRepository scheduleRepository;
    private final RouteService routeService;

    @Scheduled(fixedRate = 300000) // 5분마다
    @Transactional(readOnly = true)
    public void processRouteInformation() {
        log.info("Starting route information processing");

        // 해당하는 day 다 가져오기
        List<Day> days = dayRepository.findAll();
        List<RouteResponse> responses = new ArrayList<>();

        for (Day day : days) {
            // 해당 day의 모든 스케줄을 position_path 순으로 정렬하여 조회
            List<Schedule> orderedSchedules = scheduleRepository
                    .findAllByDayIdOrderByPositionPathAsc(day.getId());

            // 연속된 스케줄 쌍에 대해 route 정보 생성
            for (int i = 0; i < orderedSchedules.size() - 1; i++) {
                Schedule fromSchedule = orderedSchedules.get(i);
                Schedule toSchedule = orderedSchedules.get(i + 1);

            }
    }

}
}