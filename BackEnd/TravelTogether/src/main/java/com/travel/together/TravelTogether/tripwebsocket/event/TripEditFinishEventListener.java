package com.travel.together.TravelTogether.tripwebsocket.event;

import com.travel.together.TravelTogether.trip.entity.Day;
import com.travel.together.TravelTogether.trip.entity.Schedule;
import com.travel.together.TravelTogether.trip.entity.Trip;
import com.travel.together.TravelTogether.trip.repository.DayRepository;
import com.travel.together.TravelTogether.trip.repository.ScheduleRepository;
import com.travel.together.TravelTogether.trip.repository.TripRepository;
import com.travel.together.TravelTogether.tripwebsocket.cache.TripEditCache;
import com.travel.together.TravelTogether.tripwebsocket.config.TripScheduleWebSocketHandler;
import com.travel.together.TravelTogether.tripwebsocket.dto.AddRequest;
import com.travel.together.TravelTogether.tripwebsocket.dto.EditRequest;
import com.travel.together.TravelTogether.tripwebsocket.service.TripStateManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
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
    private final TripScheduleWebSocketHandler webSocketHandler;


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
                    case "ADD":
                        addSchedule(edit);
                        log.info("ADD operation queued for scheduleId: {}", edit.getOperation().getScheduleId());
                        break;
                    case "EDIT":
                        AddRequest.ScheduleDto scheduleDto = stateManager.getEditSchedule(tripId, edit.getOperation().getScheduleId());
                        if (scheduleDto != null) {
                            updateSchedule(tripId, scheduleDto);
                            log.info("EDIT operation processed for scheduleId: {}", scheduleDto.getScheduleId());
                        }
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
            throw new RuntimeException("Failed to process trip edits", e);  // 82번째줄
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

    private void addSchedule(EditRequest edit) {
        log.info("DB UPdate-===========================");
        Integer newPosition = edit.getOperation().getPositionPath();
        Integer dayId = newPosition / 10000;  // 예: 17000 / 10000 = 1
        Integer tripId = edit.getTripId();

        // 임시 저장된 AddRequest 정보 가져오기
        AddRequest addRequest = stateManager.getPendingAddRequest(tripId);
        if (addRequest == null) {
            throw new RuntimeException("AddRequest not found for tripId: " + tripId);
        }
        log.info("Pending AddRequest for tripId {}: {}", tripId, stateManager.getPendingAddRequest(tripId));

        // Day와 Trip 엔티티 조회
        Day day = dayRepository.findById(dayId)
                .orElseThrow(() -> new EntityNotFoundException("Day not found: " + dayId));
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new EntityNotFoundException("Trip not found: " + tripId));

        // EditRequest에서 필요한 정보 추출 (AddRequest의 정보가 EditRequest에 포함되어 있어야 함)
        AddRequest.ScheduleDto scheduleDto = addRequest.getSchedule();
        Schedule schedule = new Schedule(
                null,  // id는 DB에서 자동 생성
                day,
                trip,
                scheduleDto.getPlaceName(),
                1,     // orderNum
                scheduleDto.getLat(),
                scheduleDto.getLng(),
                scheduleDto.getType(),
                newPosition,
                scheduleDto.getDuration()
        );

        Schedule savedSchedule = scheduleRepository.save(schedule);

        log.info("Added new schedule: scheduleId={}, position={}",
                savedSchedule.getId(), newPosition);
    }



    private void updateSchedule(Integer tripId, AddRequest.ScheduleDto scheduleDto) {
        Schedule schedule = scheduleRepository.findById(scheduleDto.getScheduleId())
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        schedule.setDuration(scheduleDto.getDuration());
        schedule.setPlaceName(scheduleDto.getPlaceName());

        scheduleRepository.save(schedule);

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
