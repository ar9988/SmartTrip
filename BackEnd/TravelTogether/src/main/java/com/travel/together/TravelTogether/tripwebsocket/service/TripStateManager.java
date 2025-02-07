package com.travel.together.TravelTogether.tripwebsocket.service;

import com.travel.together.TravelTogether.aiPlanning.dto.DirectionsRequestDto;
import com.travel.together.TravelTogether.aiPlanning.dto.DirectionsResponseDto;
import com.travel.together.TravelTogether.aiPlanning.service.DirectionsService;
import com.travel.together.TravelTogether.trip.entity.Schedule;
import com.travel.together.TravelTogether.trip.repository.ScheduleRepository;
import com.travel.together.TravelTogether.tripwebsocket.dto.EditRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
    private final DirectionsService directionsService;
    private final ScheduleRepository scheduleRepository;

    public TripStateManager(DirectionsService directionsService, ScheduleRepository scheduleRepository, Map<Integer, List<EditRequest>> tripEditHistory, Map<Integer, Map<Integer, Integer>> tripSchedulePositions) {
        this.directionsService = directionsService;
        this.scheduleRepository = scheduleRepository;
        this.tripEditHistory = tripEditHistory;
        this.tripSchedulePositions = tripSchedulePositions;
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


    // Path 정보를 담을 클래스
    @Getter
    @Setter
    public static class PathInfo {
        private final Integer sourceScheduleId;
        private final Integer targetScheduleId;
        private final List<List<Double>> path;


        public PathInfo(Integer sourceScheduleId, Integer targetScheduleId, List<List<Double>> path) {
            this.sourceScheduleId = sourceScheduleId;
            this.targetScheduleId = targetScheduleId;
            this.path = path;

        }
    }

    // position 기준으로 정렬된 schedule들의 path 정보 생성
    public List<PathInfo> generatePathInfo(Integer tripId) {
        Map<Integer, Integer> positions = tripSchedulePositions.get(tripId);
        if (positions == null || positions.isEmpty()) {
            return new ArrayList<>();
        }

        // position 기준으로 정렬된 scheduleId 리스트 얻기
        List<Integer> orderedScheduleIds = positions.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<PathInfo> paths = new ArrayList<>();
        for (int i = 0; i < orderedScheduleIds.size() - 1; i++) {
            Schedule source = scheduleRepository.findById(orderedScheduleIds.get(i)).orElse(null);
            Schedule target = scheduleRepository.findById(orderedScheduleIds.get(i + 1)).orElse(null);

            if (source != null && target != null) {
                DirectionsRequestDto request = DirectionsRequestDto.builder()
                        .startLongitude(source.getLng())
                        .startLatitude(source.getLat())
                        .endLongitude(target.getLng())
                        .endLatitude(target.getLat())
                        .build();

                DirectionsResponseDto response = directionsService.getDrivingDirections(request);
//                paths.add(new PathInfo(source.getId(), target.getId(), response.getPath()));
            }

        }

        return paths;
    }

    private Schedule findScheduleById(List<Schedule> schedules, Integer scheduleId) {
        return schedules.stream()
                .filter(schedule -> schedule.getId().equals(scheduleId))
                .findFirst()
                .orElse(null);
    }










    // 웹소켓 연결이 끊기면 모든 작업내역 삭제
    public synchronized void clearEditHistory(Integer tripId) {
        tripEditHistory.remove(tripId);
        tripSchedulePositions.remove(tripId);
    }



}
