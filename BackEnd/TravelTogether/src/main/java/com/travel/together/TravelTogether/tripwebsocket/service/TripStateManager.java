package com.travel.together.TravelTogether.tripwebsocket.service;

import com.travel.together.TravelTogether.aiPlanning.dto.DirectionsRequestDto;
import com.travel.together.TravelTogether.aiPlanning.dto.DirectionsResponseDto;
import com.travel.together.TravelTogether.aiPlanning.service.DirectionsService;
import com.travel.together.TravelTogether.trip.entity.Schedule;
import com.travel.together.TravelTogether.trip.repository.ScheduleRepository;
import com.travel.together.TravelTogether.tripwebsocket.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
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

    // Edit 작업 전용 저장소 (tripId -> scheduleId -> Schedule)
    private final Map<Integer, Map<Integer, AddRequest.ScheduleDto>> tripEdits = new ConcurrentHashMap<>();

    // DELETE 관리용
    private final Map<Integer, Set<Integer>> tripDeletedSchedules = new ConcurrentHashMap<>();


    // ADD요청 관리용
    private final Map<Integer, AddRequest> pendingAddRequests = new ConcurrentHashMap<>();

    // TripStateManager에 메소드 추가
    public void storePendingAddRequest(Integer tripId, AddRequest addRequest) {
        pendingAddRequests.put(tripId, addRequest);
    }

    public AddRequest getPendingAddRequest(Integer tripId) {
        return pendingAddRequests.remove(tripId);  // 조회 후 삭제
    }

    // Edit 작업 내용 저장 (기존 addEdit과 별도)
    public synchronized void addEditSchedule(Integer tripId, AddRequest.ScheduleDto schedule) {
        Map<Integer, AddRequest.ScheduleDto> schedules = tripEdits.computeIfAbsent(tripId,
                k -> new ConcurrentHashMap<>());
        schedules.put(schedule.getScheduleId(), schedule);
    }

    // Edit 작업 조회
    public AddRequest.ScheduleDto getEditSchedule(Integer tripId, Integer scheduleId) {
        Map<Integer, AddRequest.ScheduleDto> schedules = tripEdits.get(tripId);
        return schedules != null ? schedules.get(scheduleId) : null;
    }


    // 삭제된 scheduleId들을 가져오는 getter
    public Set<Integer> getDeletedSchedules(Integer tripId) {
        return tripDeletedSchedules.getOrDefault(tripId, Collections.emptySet());
    }


    // DB에서 읽어온 tripDetail 저장
    private final Map<Integer, TripDetailDTO> tripDetailMap = new ConcurrentHashMap<>();

    // getter 추가
    public TripDetailDTO getTripDetail(Integer tripId) {
        log.info("getTripDetail called with tripId: {} -> result: {}",
                tripId, tripDetailMap.get(tripId));  // 로그 추가

        return tripDetailMap.get(tripId);
    }


    // positionPath 경계값 체크 및 보정
    private Integer validateAndCorrectPosition(Integer position) {
        // 경계값이면 무조건 +1
        return position % 10000 == 0 ? position + 500 : position;

    }

    public void initializeFromTripDetail(Integer tripId, TripDetailDTO tripDetail) {

        // tripDetail 저장
        tripDetailMap.put(tripId, tripDetail);

        Map<Integer, ScheduleDTO> scheduleMap = new ConcurrentHashMap<>();
        Map<Integer, Integer> positionMap = new ConcurrentHashMap<>(); // position 저장용


        // DayDto에서 모든 schedule 추출하여 Map으로 저장 (scheduleId를 key로)
        for (DayDto dayDto : tripDetail.getDayDtos()) {
            for (ScheduleDTO schedule : dayDto.getSchedules()) {
                log.info("valiadte position");

                // position 경계값 체크 및 보정
                Integer correctedPosition = validateAndCorrectPosition(schedule.getPositionPath());
                schedule.setPositionPath(correctedPosition);


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

        // 경계값 체크 및 보정
        Integer correctedPosition = validateAndCorrectPosition(positionPath);
        if (!correctedPosition.equals(positionPath)) {
            log.info("Position corrected from {} to {}", positionPath, correctedPosition);
            positionPath = correctedPosition;
        }

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
        Map<Integer, Integer> positions = tripSchedulePositions.get(tripId);
        if (positions == null) {
            log.info("No positions found for tripId: {}. Creating new map.", tripId);
            positions = new ConcurrentHashMap<>();
            tripSchedulePositions.put(tripId, positions);
        }
        return positions;
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

        // 삭제된 스케줄 기록
        tripDeletedSchedules.computeIfAbsent(tripId, k -> ConcurrentHashMap.newKeySet())
                .add(scheduleId);

        // position 제거
        Map<Integer, Integer> schedulePositions = tripSchedulePositions.get(tripId);
        if (schedulePositions != null) {
            schedulePositions.remove(scheduleId);
        }

        // 실제 스케줄 객체도 제거 (tripScheduleMap에서)
        Map<Integer, ScheduleDTO> schedules = tripScheduleMap.get(tripId);
        if (schedules != null) {
            schedules.remove(scheduleId);
            log.debug("완전히 삭제됨: tripId: {}, scheduleId: {}", tripId, scheduleId);
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
            // 경계값 체크 및 보정
            Integer positionPath = schedule.getPositionPath();
            Integer correctedPosition = validateAndCorrectPosition(positionPath);

            if (!correctedPosition.equals(positionPath)) {
                log.info("Position corrected from {} to {} during initialization",
                        positionPath, correctedPosition);
            }

            // 보정된 position 저장
            schedulePositions.put(schedule.getId(), correctedPosition);
        }


        log.info("Initialized positions for tripId {}: {}", tripId, schedulePositions);
    }

    public Integer getScheduleType(Integer tripId, Integer scheduleId) {
        // tripScheduleMap에서 스케줄 정보 조회
        Map<Integer, ScheduleDTO> scheduleMap = tripScheduleMap.get(tripId);
        if (scheduleMap != null && scheduleMap.containsKey(scheduleId)) {
            return scheduleMap.get(scheduleId).getType();
        }

        // 편집 중인 스케줄인 경우 tripEdits에서 조회
        Map<Integer, AddRequest.ScheduleDto> editMap = tripEdits.get(tripId);
        if (editMap != null && editMap.containsKey(scheduleId)) {
            return editMap.get(scheduleId).getType();
        }

        return null;  // 찾을 수 없는 경우
    }



    private Schedule findScheduleById(List<Schedule> schedules, Integer scheduleId) {
        return schedules.stream()
                .filter(schedule -> schedule.getId().equals(scheduleId))
                .findFirst()
                .orElse(null);
    }








    private static final int BATCH_SIZE = 4; // 한번에 전달할 경로 개수

    @Async
    public void generateAllPaths(Integer tripId, PathGenerationCallback callback) {
        log.info("=== START generateAllPaths for tripId: {} ===", tripId);

        Map<Integer, Integer> positions = tripSchedulePositions.get(tripId);
        if (positions == null || positions.isEmpty()) {
            callback.onPathGenerated(new ArrayList<>());
            return;
        }

        try {
            // {1=10001,2=20001}에서 value만 가져와서 정렬후 모아놓기
            List<Integer> orderedScheduleIds = positions.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            // 한번에 모든 스케줄 정보 조회
            List<Schedule> schedules = scheduleRepository.findAllById(orderedScheduleIds);
            Map<Integer, Schedule> scheduleMap = schedules.stream()
                    .collect(Collectors.toMap(Schedule::getId, schedule -> schedule));

            // 결과를 저장할 Queue
            BlockingQueue<PathInfo> pathQueue = new LinkedBlockingQueue<>();

            // CompletableFuture를 사용하여 비동기적으로 경로 생성
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            AtomicInteger expectedPathCount = new AtomicInteger(0); // 경로 개수 카운트

            // 연속된 일정 간의 모든 경로 생성
            for (int i = 0; i < orderedScheduleIds.size() - 1; i++) {
                Schedule currentSchedule = scheduleMap.get(orderedScheduleIds.get(i));
                Schedule nextSchedule = scheduleMap.get(orderedScheduleIds.get(i + 1));

                // 현재 일정이나 다음 일정이 휴식(type=2)인 경우 path 생성 건너뛰기
                if (currentSchedule.getType() == 2 || nextSchedule.getType() == 2) {
                    log.info("type=2인 schedule 스킵: {} -> {}",
                            currentSchedule.getId(), nextSchedule.getId());
                    continue;
                }

                int currentPosition = positions.get(orderedScheduleIds.get(i));
                int nextPosition = positions.get(orderedScheduleIds.get(i + 1));

                if (currentPosition / 10000 == nextPosition / 10000) {
                    expectedPathCount.incrementAndGet();
                    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                        PathInfo pathInfo = generatePath(currentSchedule, nextSchedule);
                        if (pathInfo != null) {
                            pathQueue.offer(pathInfo);
                            processBatchIfReady(pathQueue, callback, expectedPathCount.get());
                        }
                    });
                    futures.add(future);
                }
            }

            // 모든 경로 생성 완료 대기
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenRun(() -> {
                        // 남은 경로들 처리
                        List<PathInfo> remainingPaths = new ArrayList<>();
                        pathQueue.drainTo(remainingPaths);
                        if (!remainingPaths.isEmpty()) {
                            callback.onPathGenerated(remainingPaths);
                        }
                    })
                    .exceptionally(throwable -> {
                        log.error("Error in path generation", throwable);
                        return null;
                    });

        } catch (Exception e) {
            log.error("Error in generateAllPaths for tripId {}", tripId, e);
            callback.onPathGenerated(new ArrayList<>());
        }
    }

    private synchronized void processBatchIfReady(BlockingQueue<PathInfo> queue,
                                                  PathGenerationCallback callback,
                                                  int totalExpectedPaths) {
        List<PathInfo> batch = new ArrayList<>();
        if (queue.size() >= BATCH_SIZE || queue.size() == totalExpectedPaths) {
            queue.drainTo(batch, BATCH_SIZE);
            if (!batch.isEmpty()) {
                callback.onPathGenerated(batch);
                log.info("Sent batch of {} paths", batch.size());
            }
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
                Schedule prevSchedule = scheduleMap.get(prevScheduleId);

                // 이전 스케줄이나 이동된 스케줄이 휴식인 경우 제외
                if (prevSchedule.getType() != 2 && movedSchedule.getType() != 2) {
                    PathInfo prevPath = generatePath(prevSchedule, movedSchedule);
                    if (prevPath != null) {
                        paths.add(prevPath);
                    }
                } else {
                    log.info("type=2인 schedule 스킵 - prev: {}, moved: {}",
                            prevScheduleId, movedScheduleId);
                }

            }

            // 다음 스케줄과의 경로
            if (nextScheduleId != null && scheduleMap.containsKey(nextScheduleId)) {
                Schedule nextSchedule = scheduleMap.get(nextScheduleId);
                // 다음 스케줄이나 이동된 스케줄이 휴식인 경우 제외
                if (nextSchedule.getType() != 2 && movedSchedule.getType() != 2) {
                    PathInfo nextPath = generatePath(movedSchedule, nextSchedule);
                    if (nextPath != null) {
                        paths.add(nextPath);
                    }
                } else {
                    log.info("type=2인 schedule 스킵 - moved: {}, next: {}",
                            movedScheduleId, nextScheduleId);
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
    @Async
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



    // ADD용 path생성
//    @Async
    public void generatePathWithCallback(Schedule source, Schedule target, PathGenerationCallback callback) {
        log.info("Starting path generation with callback for schedules {} -> {}",
                source.getId(), target.getId());

        PathInfo path = generatePath(source, target);  // 기존 메서드 활용
        List<PathInfo> paths = new ArrayList<>();
        if (path != null) {
            paths.add(path);
        }

        callback.onPathGenerated(paths);
        log.info("Path generation callback completed for schedules {} -> {}",
                source.getId(), target.getId());
    }


    private final Map<Integer, Map<Integer, EditedScheduleInfo>> editedSchedules = new ConcurrentHashMap<>();

    // ✅ 특정 tripId의 schedule 수정 내역 저장
    public void saveEdit(Integer tripId, Integer scheduleId, ScheduleDTO updatedSchedule) {
        EditedScheduleInfo editInfo = new EditedScheduleInfo(
                updatedSchedule.getDuration(),
                updatedSchedule.getPlaceName()
        );

        editedSchedules
                .computeIfAbsent(tripId, k -> new ConcurrentHashMap<>())
                .put(scheduleId, editInfo);

        log.info("Saved edit for tripId: {}, scheduleId: {}", tripId, scheduleId);
    }




    // 웹소켓 초기 동기화용
    public TripDetailDTO getTripDetailWithEdits(Integer tripId) {
        log.info("getTripDetailWithEdits called with tripId: {}", tripId);

        TripDetailDTO tripDetail = getTripDetail(tripId);
        log.info("tripDetail is null for tripId: {}", tripId);

        if (tripDetail == null) {
            return null;
        }

        Map<Integer, EditedScheduleInfo> edits = editedSchedules.get(tripId);
        log.info("Edits for tripId {}: {}", tripId, edits);

        if (edits == null || edits.isEmpty()) {
            return tripDetail;
        }




        // tripDetail 자체는 수정하지 않고, schedule의 필드만 업데이트
        for (DayDto day : tripDetail.getDayDtos()) {
            for (ScheduleDTO schedule : day.getSchedules()) {
                EditedScheduleInfo editInfo = edits.get(schedule.getId());
                if (editInfo != null) {
                    // 수정 가능한 필드만 업데이트
                    schedule.setDuration(editInfo.getDuration());
                    schedule.setPlaceName(editInfo.getPlaceName());
                }
            }
        }

        log.info("Returning updated tripDetail for tripId: {}", tripId);
        return tripDetail;

    }

    //DELETE 후 PATH생성
    @Async
    public void generatePathsAfterDelete(Integer tripId, Integer deletedPosition, PathGenerationCallback callback) {
        log.info("=== START generatePathsAfterDelete for tripId: {}, deletedPosition: {} ===", tripId, deletedPosition);

        Map<Integer, Integer> positions = tripSchedulePositions.get(tripId);
        if (positions == null || positions.isEmpty()) {
            callback.onPathGenerated(new ArrayList<>());
            return;
        }

        try {
            // 삭제된 스케줄과 같은 날짜의 스케줄들만 고려
            int deletedDay = deletedPosition / 10000;

            // 이전/다음 스케줄 찾기
            Integer prevScheduleId = null;
            Integer nextScheduleId = null;
            Integer prevPosition = Integer.MIN_VALUE;
            Integer nextPosition = Integer.MAX_VALUE;

            for (Map.Entry<Integer, Integer> entry : positions.entrySet()) {
                int scheduleId = entry.getKey();
                int position = entry.getValue();
                int day = position / 10000;

                // 같은 날짜인 경우만 처리
                if (day == deletedDay) {
                    // 이전 스케줄 찾기
                    if (position < deletedPosition && position > prevPosition) {
                        prevPosition = position;
                        prevScheduleId = scheduleId;
                    }

                    // 다음 스케줄 찾기
                    if (position > deletedPosition && position < nextPosition) {
                        nextPosition = position;
                        nextScheduleId = scheduleId;
                    }
                }
            }

            log.info("삭제 후 주변 스케줄 - 이전: {}, 다음: {}", prevScheduleId, nextScheduleId);

            // 이전과 다음 스케줄이 모두 존재하는 경우만 처리
            if (prevScheduleId != null && nextScheduleId != null) {
                // 필요한 스케줄들 조회
                List<Schedule> schedules = scheduleRepository.findAllById(Arrays.asList(prevScheduleId, nextScheduleId));
                Map<Integer, Schedule> scheduleMap = schedules.stream()
                        .collect(Collectors.toMap(Schedule::getId, schedule -> schedule));

                // 두 스케줄 모두 조회되었고, 둘 다 type=2(휴식)가 아닌 경우에만 경로 생성
                if (scheduleMap.size() == 2 &&
                        scheduleMap.get(prevScheduleId).getType() != 2 &&
                        scheduleMap.get(nextScheduleId).getType() != 2) {

                    PathInfo pathInfo = generatePath(scheduleMap.get(prevScheduleId), scheduleMap.get(nextScheduleId));
                    if (pathInfo != null) {
                        callback.onPathGenerated(Collections.singletonList(pathInfo));
                        return;
                    }
                } else {
                    log.info("휴식 타입 스케줄이 있어 경로 생성 생략 - prevType: {}, nextType: {}",
                            scheduleMap.containsKey(prevScheduleId) ? scheduleMap.get(prevScheduleId).getType() : "없음",
                            scheduleMap.containsKey(nextScheduleId) ? scheduleMap.get(nextScheduleId).getType() : "없음");
                }
            }

            // 경로를 생성할 수 없는 경우
            callback.onPathGenerated(new ArrayList<>());

        } catch (Exception e) {
            log.error("Error in generatePathsAfterDelete for tripId {}, deletedPosition {}",
                    tripId, deletedPosition, e);
            callback.onPathGenerated(new ArrayList<>());
        }
    }



// 웹소켓 연결이 끊기면 모든 작업내역 삭제
    public synchronized void clearEditHistory(Integer tripId) {
        tripEditHistory.remove(tripId);
        tripSchedulePositions.remove(tripId);
        tripDeletedSchedules.remove(tripId);

    }







}
