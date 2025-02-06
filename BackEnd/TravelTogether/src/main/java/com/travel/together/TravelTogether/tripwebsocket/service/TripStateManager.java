package com.travel.together.TravelTogether.tripwebsocket.service;

import com.travel.together.TravelTogether.tripwebsocket.dto.EditRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TripStateManager {
    public TripStateManager() {
        this.tripEditHistory = new ConcurrentHashMap<>();
        this.tripSchedulePositions = new ConcurrentHashMap<>();
    }

    // 전체 작업 내용 저장
    private final Map<Integer, List<EditRequest>> tripEditHistory;

    // schedule position 관리 (tripId -> (scheduleId -> position))
    private final Map<Integer, Map<Integer, Integer>> tripSchedulePositions;


    // 작업 내용 추가
    public synchronized void addEdit(Integer tripId, EditRequest editRequest) {
        List<EditRequest> editHistory = tripEditHistory.computeIfAbsent(tripId,
                k -> new CopyOnWriteArrayList<>());
        editHistory.add(editRequest);
    }

    // Position 업데이트
    public synchronized void updateState(Integer tripId, Integer scheduleId, Integer positionPath) {
        log.info("Updating state - tripId: {}, scheduleId: {}, positionPath: {}",
                tripId, scheduleId, positionPath);

        Map<Integer, Integer> schedulePositions = tripSchedulePositions.computeIfAbsent(tripId,
                k -> new ConcurrentHashMap<>());
        schedulePositions.put(scheduleId, positionPath);
    }

    // 특정 trip의 전체 작업 내역 조회
    public List<EditRequest> getEditHistory(Integer tripId) {
        return new ArrayList<>(tripEditHistory.getOrDefault(tripId, new ArrayList<>()));
    }

    // 특정 trip의 schedule positions 조회
    public Map<Integer, Integer> getSchedulePositions(Integer tripId) {
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

    // schedule 삭제
    public synchronized void removeState(Integer tripId, Integer scheduleId) {
        log.debug("Removing state for tripId: {}, scheduleId: {}", tripId, scheduleId);

        Map<Integer, Integer> schedulePositions = tripSchedulePositions.get(tripId);
        if (schedulePositions != null) {
            schedulePositions.remove(scheduleId);
        }
    }



}
