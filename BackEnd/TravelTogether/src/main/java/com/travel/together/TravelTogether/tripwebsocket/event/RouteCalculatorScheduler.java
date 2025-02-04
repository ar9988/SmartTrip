package com.travel.together.TravelTogether.tripwebsocket.event;


import com.travel.together.TravelTogether.trip.entity.Day;
import com.travel.together.TravelTogether.trip.entity.Schedule;
import com.travel.together.TravelTogether.trip.repository.DayRepository;
import com.travel.together.TravelTogether.trip.repository.ScheduleRepository;
import com.travel.together.TravelTogether.tripwebsocket.config.TripScheduleWebSocketHandler;
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
import org.springframework.web.socket.TextMessage;

@Slf4j
@Component
@RequiredArgsConstructor
public class RouteCalculatorScheduler {
    private final TripScheduleWebSocketHandler webSocketHandler;
    private final DayRepository dayRepository;
    private final ScheduleRepository scheduleRepository;
    private final RouteService routeService;

    @Scheduled(fixedRate = 30000) // 30초마다
    @Transactional(readOnly = true)
    public void processRouteInformation() {
        log.info("Starting route information processing");

        // 해당하는 day 다 가져오기
        List<Day> days = dayRepository.findAll();
        log.info("Days found: {}", days.size());

        for (Day day : days) {
            // 해당 day의 모든 스케줄을 position_path 순으로 정렬하여 조회
            List<Schedule> orderedSchedules = scheduleRepository
                    .findAllByDayIdOrderByPositionPathAsc(day.getId());

            if (orderedSchedules.size() < 2) {
                continue; // 경로 계산이 필요없는 경우 스킵
            }

            // route 응답준비
            RouteResponse response = new RouteResponse();
            response.setTripId(day.getTrip().getId());
            List<RouteResponse.Routes> routes = new ArrayList<>();


            // 연속된 스케줄 쌍에 대해 route 정보 생성
            for (int i = 0; i < orderedSchedules.size() - 1; i++) {
                Schedule fromSchedule = orderedSchedules.get(i);
                Schedule toSchedule = orderedSchedules.get(i + 1);

            // TODO: 위도,경도에 따른 루트 계산
                RouteService.Coordinate startCoord = new RouteService.Coordinate();
                startCoord.setLatitude(fromSchedule.getLat());
                startCoord.setLongitude(fromSchedule.getLng());

                RouteService.Coordinate endCoord = new RouteService.Coordinate();
                endCoord.setLatitude(toSchedule.getLat());
                endCoord.setLongitude(toSchedule.getLng());

                RouteResponse.Routes route = routeService.calculateRoute(
                        startCoord,
                        endCoord,
                        day.getId());

                routes.add(route);
            }
            response.setRoutes(routes);

            // WebSocket으로 계산된 route 정보 전송
            String tripId = String.valueOf(day.getTrip().getId());
            webSocketHandler.broadcastRouteInfo(tripId, response);


            log.debug("Route calculation completed for day: {}, trip: {}",
                    day.getId(), day.getTrip().getId());
        }

}
}