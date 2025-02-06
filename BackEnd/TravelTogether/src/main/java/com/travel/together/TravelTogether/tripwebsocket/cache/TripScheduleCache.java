package com.travel.together.TravelTogether.tripwebsocket.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


// schedule의 positionPath저장
@Component
@Slf4j
public class TripScheduleCache {
    // tripId -> (scheduleId -> position)
    private final Map<String, Map<Integer, Integer>> tripSchedulePositions;  // tripId -> (scheduleId -> position)

    public TripScheduleCache() {
        this.tripSchedulePositions = new ConcurrentHashMap<>();
    }

    // position 값 업데이트
    public synchronized void updatePosition(String tripId, Integer scheduleId, Integer positionPath) {
        System.out.println("Updating position: " + tripId + ", " + scheduleId + ", " + positionPath);

        Map<Integer, Integer> schedulePositions = tripSchedulePositions.computeIfAbsent(tripId,
                k -> new ConcurrentHashMap<>());
        schedulePositions.put(scheduleId, positionPath);

        log.debug("ScheduleCache.updatePosition called with params - tripId: {}, scheduleId: {}, positionPath: {}",
                tripId, scheduleId, positionPath);

    }

    // 특정 trip의 모든 schedule positions 조회
    public Map<Integer, Integer> getSchedulePositions(String tripId) {
        return new HashMap<>(tripSchedulePositions.getOrDefault(tripId, new HashMap<>()));
    }

    // position 기준으로 정렬된 scheduleId 리스트 반환
    public List<Integer> getOrderedScheduleIds(String tripId) {
        Map<Integer, Integer> positions = tripSchedulePositions.get(tripId);
        if (positions == null) {
            return new ArrayList<>();
        }

        log.info("Getting ordered scheduleIds for tripId: {}", tripId);
        log.info("Current positions map: {}", positions);

        return positions.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    // 삭제
    public synchronized void removePosition(String tripId, Integer scheduleId) {
        log.debug("Removing position for tripId: {}, scheduleId: {}", tripId, scheduleId);

        Map<Integer, Integer> schedulePositions = tripSchedulePositions.get(tripId);
        if (schedulePositions != null) {
            schedulePositions.remove(scheduleId);

            // 만약 Map이 비었으면 trip 자체를 삭제
//            if (schedulePositions.isEmpty()) {
//                tripSchedulePositions.remove(tripId);
//            }
        }
    }

}
