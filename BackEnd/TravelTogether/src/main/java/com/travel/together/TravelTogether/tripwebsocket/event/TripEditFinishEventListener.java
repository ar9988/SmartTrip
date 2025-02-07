package com.travel.together.TravelTogether.tripwebsocket.event;

import com.travel.together.TravelTogether.trip.entity.Schedule;
import com.travel.together.TravelTogether.trip.repository.DayRepository;
import com.travel.together.TravelTogether.trip.repository.ScheduleRepository;
import com.travel.together.TravelTogether.trip.repository.TripRepository;
import com.travel.together.TravelTogether.tripwebsocket.dto.EditRequest;
import com.travel.together.TravelTogether.tripwebsocket.cache.TripEditCache;
import com.travel.together.TravelTogether.tripwebsocket.service.TripStateManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import lombok.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class TripEditFinishEventListener {
    private final TripRepository tripRepository;
    private final TripEditCache editCache;
    private final DayRepository dayRepository;
    private final ScheduleRepository scheduleRepository;
    private final TripStateManager stateManager;


    @Async  // 비동기로 처리
    @EventListener
    @Transactional
    public void handleTripEditFinish(TripEditFinishEvent event) {
        Integer tripId = event.getTripId();
        log.info("processing edit history for tripID: {}", tripId);

        // 메모리에서 해당 trip의 모든 편집내용 가져오기
        List<EditRequest> edits = stateManager.getEditHistory(tripId);

        if (edits == null || edits.isEmpty()) {
            log.info("No edits found for tripId: {}", tripId);
            return;
        }

        try {
            for (EditRequest edit : edits) {
                switch (edit.getOperation().getAction()) {
                    case "MOVE":
                        updateSchedulePosition(edit);
                        break;
                    case "DELETE":
                        deleteSchedule(edit);
                        break;
                    default:
                        log.warn("Unknown operation: {}", edit.getOperation().getAction());
                }

                log.info("Successfully processed {} edits for tripId: {}", Optional.of(edits.size()), tripId);

            }
            // 모든 처리가 끝난 후 캐시 정리
            stateManager.clearEditHistory(tripId);

            log.info("Successfully processed {} edits for tripId: {}", edits.size(), tripId);




        } catch (Exception e) {
            log.error("Error processing edits for tripId: {}", tripId, e);
            throw new RuntimeException("Failed to process trip edits", e);
        }

    }


    private void updateSchedulePosition(EditRequest edit) {
        Integer scheduleId = edit.getOperation().getScheduleId();
        Integer newPosition = edit.getOperation().getPositionPath();

        // 이동할 스케줄 가져오기
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("Schedule not found: " + scheduleId));


        // 모든 변경사항 저장
        schedule.setPositionPath(newPosition);
        scheduleRepository.save(schedule);
//        scheduleRepository.saveAll(schedules);


        log.info("Updated schedule position: scheduleId={}, newPosition={}", scheduleId, newPosition);
    }


    private void deleteSchedule(EditRequest edit) {
        Integer scheduleId = edit.getOperation().getScheduleId();

        // 스케줄이 존재하는지 확인
        if (scheduleRepository.existsById(scheduleId)) {
            scheduleRepository.deleteById(scheduleId);
            log.info("Deleted schedule: {}", scheduleId);

        } else {
            log.warn("Attempted to delete non-existent schedule: {}", scheduleId);
        }
    }

}
