package com.travel.together.TravelTogether.tripwebsocket.service;

import com.travel.together.TravelTogether.aiPlanning.dto.DirectionsRequestDto;
import com.travel.together.TravelTogether.aiPlanning.dto.DirectionsResponseDto;
import com.travel.together.TravelTogether.aiPlanning.service.DirectionsService;
import com.travel.together.TravelTogether.trip.entity.Schedule;
import com.travel.together.TravelTogether.trip.repository.ScheduleRepository;
import com.travel.together.TravelTogether.tripwebsocket.dto.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
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

    // DB 초기상태 저장용 Map (tripId -> (scheduleId -> ScheduleDTO))
    private final Map<Integer, Map<Integer, ScheduleDTO>> tripScheduleMap = new ConcurrentHashMap<>();


    // ADD요청 관리용
    private final Map<Integer, AddRequest> pendingAddRequests = new ConcurrentHashMap<>();

    // TripStateManager에 메소드 추가
    public void storePendingAddRequest(Integer tripId, AddRequest addRequest) {
        pendingAddRequests.put(tripId, addRequest);
    }

    public AddRequest getPendingAddRequest(Integer tripId) {
        return pendingAddRequests.remove(tripId);  // 조회 후 삭제
    }



    // DB에서 읽어온 tripDetail 저장
    private final Map<Integer, TripDetailDTO> tripDetailMap = new ConcurrentHashMap<>();

    // getter 추가
    public TripDetailDTO getTripDetail(Integer tripId) {
        log.info("getTripDetail called with tripId: {} -> result: {}",
                tripId, tripDetailMap.get(tripId));  // 로그 추가

        return tripDetailMap.get(tripId);
    }

    public void initializeFromTripDetail(Integer tripId, TripDetailDTO tripDetail) {

        // tripDetail 저장
        tripDetailMap.put(tripId, tripDetail);

        Map<Integer, ScheduleDTO> scheduleMap = new ConcurrentHashMap<>();
        Map<Integer, Integer> positionMap = new ConcurrentHashMap<>(); // position 저장용


        // DayDto에서 모든 schedule 추출하여 Map으로 저장 (scheduleId를 key로)
        for (DayDto dayDto : tripDetail.getDayDtos()) {
            for (ScheduleDTO schedule : dayDto.getSchedules()) {
                scheduleMap.put(schedule.getId(), schedule);
                positionMap.put(schedule.getId(), schedule.getPositionPath()); // position 저장

            }
        }

        tripScheduleMap.put(tripId, scheduleMap);
        tripSchedulePositions.putIfAbsent(tripId, positionMap);
        log.info("Initialized schedules for tripId {}: {} schedules", tripId, scheduleMap.size());
        log.info("Initialized schedule positions for tripId {}: {}", tripId, positionMap);

        log.info("Initialized schedules for tripId {}: {} schedules",
                tripId, scheduleMap.size());
    }

    // tripScheduleMap의 getter 추가
    public Map<Integer, Map<Integer, ScheduleDTO>> getTripScheduleMap() {
        return tripScheduleMap;
    }






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

        // scheduleMap의 DTO도 업데이트
        Map<Integer, ScheduleDTO> scheduleMap = tripScheduleMap.get(tripId);
        if (scheduleMap != null && scheduleMap.containsKey(scheduleId)) {
            ScheduleDTO schedule = scheduleMap.get(scheduleId);
            schedule.setPositionPath(positionPath);
        }

    }

    // 특정 trip의 전체 작업 내역 조회
    public List<EditRequest> getEditHistory(Integer tripId) {
        return new ArrayList<>(tripEditHistory.getOrDefault(tripId, new ArrayList<>()));
    }

    // 특정 trip의 schedule positions 조회
    public Map<Integer, Integer> getSchedulePositions(Integer tripId) {

//        return new HashMap<>(tripSchedulePositions.getOrDefault(tripId, new HashMap<>()));
        return tripSchedulePositions.get(tripId);

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

    public boolean hasPositions(Integer tripId) {
        Map<Integer, Integer> positions = tripSchedulePositions.get(tripId);
        return positions != null && !positions.isEmpty();
    }


    public void initializeSchedulePositions(Integer tripId, List<Schedule> schedules) {
        Map<Integer, Integer> schedulePositions = tripSchedulePositions.computeIfAbsent(tripId,
                k -> new ConcurrentHashMap<>());

        for (Schedule schedule : schedules) {
            schedulePositions.put(schedule.getId(), schedule.getPositionPath());
        }
        log.info("Initialized positions for tripId {}: {}", tripId, schedulePositions);
    }



    private Schedule findScheduleById(List<Schedule> schedules, Integer scheduleId) {
        return schedules.stream()
                .filter(schedule -> schedule.getId().equals(scheduleId))
                .findFirst()
                .orElse(null);
    }





    @Async
    public void generateAllPaths(Integer tripId, PathGenerationCallback callback) {
        log.info("=== START generateAllPaths for tripId: {} ===", tripId);


        Map<Integer, Integer> positions = tripSchedulePositions.get(tripId);
        if (positions == null || positions.isEmpty()) {
            callback.onPathGenerated(new ArrayList<>());
            return;
        }

        try {
            List<Integer> orderedScheduleIds = positions.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            // 한번에 모든 스케줄 정보 조회
            List<Schedule> schedules = scheduleRepository.findAllById(orderedScheduleIds);
            Map<Integer, Schedule> scheduleMap = schedules.stream()
                    .collect(Collectors.toMap(Schedule::getId, schedule -> schedule));

            List<PathInfo> paths = new ArrayList<>();

            // 연속된 일정 간의 모든 경로 생성
            for (int i = 0; i < orderedScheduleIds.size() - 1; i++) {
                int currentPosition = positions.get(orderedScheduleIds.get(i));
                int nextPosition = positions.get(orderedScheduleIds.get(i + 1));

                if (currentPosition / 10000 == nextPosition / 10000) {
                    Schedule source = scheduleMap.get(orderedScheduleIds.get(i));
                    Schedule target = scheduleMap.get(orderedScheduleIds.get(i + 1));

                    PathInfo pathInfo = generatePath(source, target);
                    if (pathInfo != null) {
                        paths.add(pathInfo);
                        log.info("Successfully added path between {} and {}", source.getId(), target.getId());

                    }
                }


            }

            log.info("Generated paths count: {}", paths.size());

            callback.onPathGenerated(paths);

        } catch (Exception e) {
            log.error("Error in generateAllPaths for tripId {}", tripId, e);
            callback.onPathGenerated(new ArrayList<>());
        }
    }


    // 특정 스케줄 이동에 대한 경로 생성 (MOVE 액션용)
    @Async
    public void generatePathsForSchedule(Integer tripId, Integer movedScheduleId, PathGenerationCallback callback) {
        log.info("=== START generatePathsForSchedule for tripId: {}, scheduleId: {} ===",
                tripId, movedScheduleId);

        Map<Integer, Integer> positions = tripSchedulePositions.get(tripId);
        if (positions == null || positions.isEmpty()) {
            callback.onPathGenerated(new ArrayList<>());
            return;
        }

        try {
            // 현재 스케줄의 위치값 찾기
            Integer currentPosition = positions.get(movedScheduleId);
            log.info("2. 현재 포지션 찾음: {}", currentPosition);

            if (currentPosition == null) {
                callback.onPathGenerated(new ArrayList<>());
                return;
            }

            // 이전/다음 스케줄 찾기
            Integer prevScheduleId = null;
            Integer nextScheduleId = null;
            Integer prevPosition = Integer.MIN_VALUE;
            Integer nextPosition = Integer.MAX_VALUE;

            for (Map.Entry<Integer, Integer> entry : positions.entrySet()) {
                int pos = entry.getValue(); // 현재 스케쥴의 위치값
                if (pos < currentPosition && pos > prevPosition) {
                    prevPosition = pos;
                    prevScheduleId = entry.getKey();
                }
                if (pos > currentPosition && pos < nextPosition) {
                    nextPosition = pos;
                    nextScheduleId = entry.getKey();
                }
            }
            log.info("3. 이전/다음 스케줄 ID 찾음 - prev: {}, next: {}", prevScheduleId, nextScheduleId);  // 추가


            // 필요한 스케줄들만 조회
            List<Integer> scheduleIds = Arrays.asList(prevScheduleId, movedScheduleId, nextScheduleId)
                    .stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            List<Schedule> schedules = scheduleRepository.findAllById(scheduleIds);
            Map<Integer, Schedule> scheduleMap = schedules.stream()
                    .collect(Collectors.toMap(Schedule::getId, schedule -> schedule));

            List<PathInfo> paths = new ArrayList<>();
            Schedule movedSchedule = scheduleMap.get(movedScheduleId);

            // 이전 스케줄과의 경로
            if (prevScheduleId != null && scheduleMap.containsKey(prevScheduleId)) {
                PathInfo prevPath = generatePath(scheduleMap.get(prevScheduleId), movedSchedule);
                if (prevPath != null) {
                    paths.add(prevPath);
                }
            }

            // 다음 스케줄과의 경로
            if (nextScheduleId != null && scheduleMap.containsKey(nextScheduleId)) {
                PathInfo nextPath = generatePath(movedSchedule, scheduleMap.get(nextScheduleId));
                if (nextPath != null) {
                    paths.add(nextPath);
                }
            }

            callback.onPathGenerated(paths);

        } catch (Exception e) {
            log.error("Error in generatePathsForSchedule for tripId {}, scheduleId {}",
                    tripId, movedScheduleId, e);
            callback.onPathGenerated(new ArrayList<>());
        }
    }

    // 경로 생성 헬퍼 메서드
    public PathInfo generatePath(Schedule source, Schedule target) {
        log.info("generatePath===========================");
        try {
            DirectionsRequestDto request = DirectionsRequestDto.builder()
                    .startLongitude(source.getLng())
                    .startLatitude(source.getLat())
                    .endLongitude(target.getLng())
                    .endLatitude(target.getLat())
                    .build();

            DirectionsResponseDto response = directionsService.getDrivingDirections(request);
            log.info("directions 응답: {}", response);

            if (response != null && response.getDirectionPath() != null) {
                List<List<Double>> coordinates = response.getDirectionPath().getPath().stream()
                        .map(point -> Arrays.asList(point.getLongitude(), point.getLatitude()))
                        .collect(Collectors.toList());

                return new PathInfo(
                        source.getId(),
                        target.getId(),
                        coordinates,
                        response.getTotalTime()
                );
            }
        } catch (Exception e) {
            log.error("Error generating path from schedule {} to {}",
                    source.getId(), target.getId(), e);
        }
        return null;
    }











    // 웹소켓 연결이 끊기면 모든 작업내역 삭제
    public synchronized void clearEditHistory(Integer tripId) {
        tripEditHistory.remove(tripId);
        tripSchedulePositions.remove(tripId);
    }







}
