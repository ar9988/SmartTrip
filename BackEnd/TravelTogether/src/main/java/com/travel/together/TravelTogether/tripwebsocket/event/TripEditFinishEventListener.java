package com.travel.together.TravelTogether.tripwebsocket.event;

import com.travel.together.TravelTogether.trip.entity.Trip;
import com.travel.together.TravelTogether.trip.repository.DayRepository;
import com.travel.together.TravelTogether.trip.repository.ScheduleRepository;
import com.travel.together.TravelTogether.trip.repository.TripRepository;
import com.travel.together.TravelTogether.tripwebsocket.dto.TripEditCache;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import lombok.*;

@Slf4j
@RequiredArgsConstructor
public class TripEditFinishEventListener {
    private final TripRepository tripRepository;
    private final TripEditCache editCache;
    private final DayRepository dayRepository;
    private final ScheduleRepository scheduleRepository;


    @Async  // 비동기로 처리
    @EventListener
    @Transactional
    public void handleTripEditFinish(TripEditFinishEvent event) {
        log.info("Handling TripEditFinishEvent for tripId: {}", event.getTripId());
        try {
            // 1. 먼저 Trip 엔티티를 조회
            Trip trip = tripRepository.findById(event.getTripId())
                    .orElseThrow(() -> new EntityNotFoundException("Trip not found: " + event.getTripId()));

            // 2. 메모리에 있던 편집 내용을 DB에 반영
            // TODO: 실제 데이터 업데이트 로직 구현

            log.info("Successfully processed TripEditFinishEvent for tripId: {}", event.getTripId());
        } catch (Exception e) {
            log.error("Error processing TripEditFinishEvent for tripId: " + event.getTripId(), e);
            // 에러 처리 로직 필요
        }
    }



}
