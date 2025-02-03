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
import org.springframework.stereotype.Component;

import java.util.List;

@Component
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
                if ("MOVE".equals(edit.getOperation().getAction())) {
                    // position_path받아서 DB업데이트
                    updateScheduleOrder(edit);
                    log.info("Updated schedule order: scheduleId={}, toPosition={}",
                            edit.getOperation().getScheduleId(),
                            edit.getOperation().getPositionPath());

                } else if ("DELETE".equals(edit.getOperation().getAction())) {
                    deleteSchedule(edit);
                    log.info("Deleted schedule: scheduleId={}",
                            edit.getOperation().getScheduleId());

                } else if ("ADD".equals(edit.getOperation().getAction())) {
                    // TODO: 일정추가기능
                }

                log.info("Successfully processed {} edits for tripId: {}", edits.size(), tripId);

            }

        } catch (Exception e) {
            log.error("Error processing edits for tripId: {}", tripId, e);
            throw new RuntimeException("Failed to process trip edits", e);
        }

    }


    private void updateScheduleOrder(EditRequest edit) {
        Integer scheduleId = edit.getOperation().getScheduleId();
        Integer positionPath = edit.getOperation().getPositionPath();

        // 이동할 스케줄 가져오기
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("Schedule not found: " + scheduleId));


        // 같은 날의 모든 스케쥴 가져오기
        List<Schedule> schedules = scheduleRepository.findAllByDayIdOrderByOrderNumAsc(schedule.getDay().getId());


        // 위치 변경을 위한 orderNum 업데이트
//        if (fromPosition < toPosition) {
//            // 앞에서 뒤로 이동하는 경우
//            for (Schedule s : schedules) {
//                if (s.getId().equals(scheduleId)) {
//                    s.setOrderNum(toPosition);
//                } else if (s.getOrderNum() <= toPosition && s.getOrderNum() > fromPosition) {
//                    s.setOrderNum(s.getOrderNum() - 1);
//                }
//            }
//        } else {
//            // 뒤에서 앞으로 이동
//            for (Schedule s : schedules) {
//                if (s.getId().equals(scheduleId)) {
//                    s.setOrderNum(fromPosition);
//                } else if (s.getOrderNum() >= toPosition && s.getOrderNum() < fromPosition) {
//                    s.setOrderNum(s.getOrderNum() + 1);
//                }
//            }
//        }



        // 모든 변경사항 저장
        schedule.setPositionPath(positionPath);
        scheduleRepository.save(schedule);
//        scheduleRepository.saveAll(schedules);

        // 변경된 모든 스케줄의 순서 로깅
        for (Schedule s : schedules) {
            log.info("Schedule id={} now has orderNum={}", s.getId(), s.getOrderNum());
        }
        log.info("Updated schedule order: scheduleId={}, finalPosition={}", scheduleId, positionPath);
    }


    private void deleteSchedule(EditRequest edit) {
        Integer scheduleId = edit.getOperation().getScheduleId();

        // 스케줄이 존재하는지 확인
        if (scheduleRepository.existsById(scheduleId)) {
            scheduleRepository.deleteById(scheduleId);
        } else {
            log.warn("Attempted to delete non-existent schedule: {}", scheduleId);
        }
    }

}
