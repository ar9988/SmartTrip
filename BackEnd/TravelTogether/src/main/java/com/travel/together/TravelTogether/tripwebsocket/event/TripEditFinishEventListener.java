package com.travel.together.TravelTogether.tripwebsocket.event;

import com.travel.together.TravelTogether.trip.entity.Schedule;
import com.travel.together.TravelTogether.trip.repository.DayRepository;
import com.travel.together.TravelTogether.trip.repository.ScheduleRepository;
import com.travel.together.TravelTogether.trip.repository.TripRepository;
import com.travel.together.TravelTogether.tripwebsocket.dto.EditRequest;
import com.travel.together.TravelTogether.tripwebsocket.dto.TripEditCache;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import lombok.*;

import java.util.List;

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
        Integer tripId = event.getTripId();
        log.info("processing edit history for tripID: {}", tripId);

        // 캐시에서 해당 trip의 모든 편집내용 가져오기
        List<EditRequest> edits = editCache.getAndRemoveEdits(String.valueOf(tripId));

        if (edits == null || edits.isEmpty()) {
            log.info("No edits found for tripId: {}", tripId);
            return;
        }

        try {
            // 편집 내역 순서대로 처리
            for (EditRequest edit : edits) {
                if ("DAY".equals(edit.getOperation().getType())) {
                   // TODO: DAY 순서 업데이트하는 로직..
                   // updateDayOrder(edit);
                } else if ("SCHEDULE".equals(edit.getOperation().getType())) {
                   // TODO: 스케쥴 순서 업데이트하는로직이라서 꼭해야함....
                    // updateScheduleOrder(edit);
                }

                log.info("Successfully processed {} edits for tripId: {}", edits.size(), tripId);

            }

        } catch (Exception e) {
            log.error("Error processing edits for tripId: {}", tripId, e);
            throw new RuntimeException("Failed to process trip edits", e);
        }

//        log.info("Handling TripEditFinishEvent for tripId: {}", event.getTripId());
//        try {
//            // 1. 먼저 Trip 엔티티를 조회
//            Trip trip = tripRepository.findById(event.getTripId())
//                    .orElseThrow(() -> new EntityNotFoundException("Trip not found: " + event.getTripId()));
//
//            // 2. 메모리에 있던 편집 내용을 DB에 반영
//            // TODO: 실제 데이터 업데이트 로직 구현
//
//            log.info("Successfully processed TripEditFinishEvent for tripId: {}", event.getTripId());
//        } catch (Exception e) {
//            log.error("Error processing TripEditFinishEvent for tripId: " + event.getTripId(), e);
//            // 에러 처리 로직 필요
//        }


    }


    private void updateScheduleOrder(EditRequest edit) {
       Integer scheduleId = edit.getOperation().getSchedule_id();
       Integer fromPosition = edit.getOperation().getFromPosition();
       Integer toPosition = edit.getOperation().getToPosition();

       Schedule schedule = scheduleRepository.findById(scheduleId)
               .orElseThrow(() -> new EntityNotFoundException("schedule is not found:" + scheduleId));

       // 같은 날의 모든 스케쥴 가져오기
        List<Schedule> schedules = scheduleRepository.findAllByDayIdOrderByOrderNumAsc(schedule.getDay().getId());

        // 만약 fromPosition < toPosition이면 fromPosition+1부터 toPosition까지 앞으로당기기
        // 위로 이동하는 경우 (예: 2→4)
        // fromPosition+1 부터 toPosition 까지의 스케줄들을 한 칸씩 앞으로 당김
        if (fromPosition < toPosition) {
            for(Schedule s : schedules) {
                if (s.getOrderNum() > fromPosition && s.getOrderNum() <= toPosition) {
                    s.setOrderNum(s.getOrderNum() - 1);
                }
            }
        } else if (fromPosition > toPosition) {
            // 아래로 이동하는 경우 (예: 4→2)
            // toPosition 부터 fromPosition-1 까지의 스케줄들을 한 칸씩 뒤로 밈
            for(Schedule s: schedules) {
                if (s.getOrderNum() >= toPosition && s.getOrderNum() < fromPosition ) {
                    s.setOrderNum(s.getOrderNum() + 1);
                }
            }
        }

        // 이동하는 스케줄의 위치를 목표 위치로 설정
        schedule.setOrderNum(toPosition);

        // 변경된 모든 스케줄 저장
        scheduleRepository.saveAll(schedules);
    }
}
