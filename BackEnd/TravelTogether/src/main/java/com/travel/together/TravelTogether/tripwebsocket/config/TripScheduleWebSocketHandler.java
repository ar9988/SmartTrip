package com.travel.together.TravelTogether.tripwebsocket.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.together.TravelTogether.trip.dto.ScheduleDto;
import com.travel.together.TravelTogether.trip.entity.Day;
import com.travel.together.TravelTogether.trip.entity.Schedule;
import com.travel.together.TravelTogether.trip.entity.Trip;
import com.travel.together.TravelTogether.trip.repository.DayRepository;
import com.travel.together.TravelTogether.trip.repository.ScheduleRepository;
import com.travel.together.TravelTogether.trip.repository.TripRepository;
import com.travel.together.TravelTogether.trip.service.TripService;
import com.travel.together.TravelTogether.tripwebsocket.cache.TripScheduleCache;
import com.travel.together.TravelTogether.tripwebsocket.dto.*;
import com.travel.together.TravelTogether.tripwebsocket.cache.TripEditCache;
import com.travel.together.TravelTogether.tripwebsocket.event.TripEditFinishEvent;
import com.travel.together.TravelTogether.tripwebsocket.service.ScheduleService;
import com.travel.together.TravelTogether.tripwebsocket.service.TripStateManager;
import io.jsonwebtoken.io.IOException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class TripScheduleWebSocketHandler extends TextWebSocketHandler {
    private final ApplicationEventPublisher eventPublisher;
    private final TripEditCache editCache;  // 추가
    private final ObjectMapper objectMapper; // JSON 파싱을 위해 추가
    private final ScheduleService scheduleService;
    private final TripScheduleCache scheduleCache;
    private final TripStateManager stateManager;
    private final TripService tripService;
    private final ScheduleRepository scheduleRepository;
    private final ExecutorService executorService;
    private final DayRepository dayRepository;
    private final TripRepository tripRepository;


    // tripId를 키로 하고, 해당 여행의 접속자들의 세션을 값으로 가지는 Map
    private final Map<String, Set<WebSocketSession>> tripSessions = new ConcurrentHashMap<>();

    // 세션 ID를 키로 하고, tripId를 값으로 가지는 Map (세션이 어느 여행에 속해있는지 추적)
    // "session123" -> "trip456", 하나의 trip에 여러 session이 매핑됨
    private final Map<String, String> sessionTripMapping = new ConcurrentHashMap<>();

    // 해당 Trip의 DayId저장용
    private final Map<Integer, List<Integer>> saveDayId = new ConcurrentHashMap<>();

    private void handleStartOperation(WebSocketSession session, Integer tripId) {
        try {
            log.info("=== START handleStartOperation for tripId: {} ===", tripId);

            // 1. Get trip details
            TripDetailDTO tripDetail = tripService.getTripDetailById(tripId);

            // 2. Initialize schedule positions if not already initialized
            if (!stateManager.hasPositions(tripId)) {
                List<Schedule> schedules = scheduleRepository.findAllByTripId(tripId);
                stateManager.initializeSchedulePositions(tripId, schedules);
            }

            // 3. Create and send initial response with trip details
            TripDetailResponse initialResponse = new TripDetailResponse(
                    tripId,
                    tripDetail.getTitle(),
                    tripDetail.getMembers(),
                    tripDetail.getDayDtos(),
                    tripDetail.getCreatedAt(),
                    tripDetail.getUpdatedAt()
            );
            String jsonResponse = objectMapper.writeValueAsString(initialResponse);
            session.sendMessage(new TextMessage(jsonResponse));

            // 경로정보 전송
            stateManager.generateAllPaths(tripId, paths -> {
                if (paths != null && !paths.isEmpty()) {
                    try {
                        Schedule firstSchedule = scheduleRepository.findFirstByTripIdOrderByPositionPathAsc(tripId);

                        log.info("Preparing MoveResponse - paths size: {}", paths.size());
                        log.info("Paths to be sent: {}", paths);

                        MoveResponse pathResponse = new MoveResponse(
                                tripId,
                                firstSchedule.getId(),
                                firstSchedule.getPositionPath(),
                                paths
                        );
                        String pathJsonResponse = objectMapper.writeValueAsString(pathResponse);
                        session.sendMessage(new TextMessage(pathJsonResponse));

                        log.info("Generated JSON response: {}", pathJsonResponse);

                    } catch (Exception e) {
                        log.error("Error sending initial paths for tripId: {}", tripId, e);
                    }
                }
            });
            log.info("=== END handleStartOperation for tripId: {} ===", tripId);
        } catch (Exception e) {
            log.error("Error in handleStartOperation for tripId: {}", tripId, e);
            try {
                session.sendMessage(new TextMessage("{\"error\": \"Failed to process START action\"}"));
            } catch (IOException | java.io.IOException ex) {
                log.error("Failed to send error message", ex);
            }
        }
    }

    // 처음 들어온 유저한테
    private void handleInitialSync(WebSocketSession session, Integer tripId) throws IOException, java.io.IOException {
        log.info("=== START Initial Sync for tripId: {} ===", tripId);

        // 1. 현재 상태 가져오기 (메모리에서)
        TripDetailDTO currentTripDetail = stateManager.getTripDetail(tripId);

        if (currentTripDetail == null) {
            log.info("TripDetail is null, initializing from DB for tripId: {}", tripId);
            // 첫 접속인 경우 DB에서 초기화
            currentTripDetail = tripService.getTripDetailById(tripId);
            if (currentTripDetail != null) {
                stateManager.initializeFromTripDetail(tripId, currentTripDetail);
                // 초기화 후 바로 이 tripDetail 사용
                log.info("Using initialized tripDetail directly");
            } else {
                log.error("No trip detail found in DB for tripId: {}", tripId);
                String errorMessage = "{\"type\": \"ERROR\", \"message\": \"Trip not found\"}";
                session.sendMessage(new TextMessage(errorMessage));
                return;
            }
        }

        // 2. 현재의 position 정보, DELETE정보 적용
        log.info("Getting schedule positions for tripId: {}", tripId);
        Map<Integer, Integer> currentPositions = stateManager.getSchedulePositions(tripId);
        log.info("Retrieved positions. Size: {}", currentPositions != null ? currentPositions.size() : 0);
        Set<Integer> deletedIds = stateManager.getDeletedSchedules(tripId);

        if (currentPositions != null) {
            for (DayDto day : currentTripDetail.getDayDtos()) {
                // 삭제된 schedule 필터링하여 새 리스트 생성
                List<ScheduleDTO> filteredSchedules = new ArrayList<>();


                for (ScheduleDTO schedule : day.getSchedules()) {

                    // 삭제된 schedule이 아닌 경우만 처리
                    if (!deletedIds.contains(schedule.getId())) {
                        Integer position = currentPositions.get(schedule.getId());
                        log.info("Schedule ID: {}, Current Position: {}", schedule.getId(), position);

                        if (position != null) {
                            schedule.setPositionPath(position);
                        }
                        filteredSchedules.add(schedule);
                    }

                }

                // 필터링된 리스트로 교체
                day.setSchedules(filteredSchedules);

            }
        }

        // 3. 초기 응답 전송
        TripDetailResponse initialResponse = new TripDetailResponse(
                tripId,
                currentTripDetail.getTitle(),
                currentTripDetail.getMembers(),
                currentTripDetail.getDayDtos(),
                currentTripDetail.getCreatedAt(),
                currentTripDetail.getUpdatedAt()
        );
        String jsonResponse = objectMapper.writeValueAsString(initialResponse);
        session.sendMessage(new TextMessage(jsonResponse));
        log.info("Sent initial trip detail for tripId: {}", tripId);

        // 4. 경로 정보 전송 (있는 경우)
        if (stateManager.hasPositions(tripId)) {
            log.info("hasPositions returned true for tripId: {}", tripId);
            sendPathInformation(session, tripId);
        }

        log.info("=== END Initial Sync for tripId: {} ===", tripId);
    }


    // 새로 들어온 유저한테 현재상테 전송
    private void sendPathInformation(WebSocketSession session, Integer tripId) {
        stateManager.generateAllPaths(tripId, paths -> {
            if (paths != null && !paths.isEmpty()) {
                try {
                    // tripScheduleMap에서 스케줄 정보 가져오기
                    Map<Integer, ScheduleDTO> scheduleMap = stateManager.getTripScheduleMap().get(tripId);
                    if (scheduleMap != null && !scheduleMap.isEmpty()) {
                        // positionPath를 기준으로 정렬하여 첫 번째 스케줄 찾기
                        ScheduleDTO firstSchedule = scheduleMap.values().stream()
                                .min(Comparator.comparing(ScheduleDTO::getPositionPath))
                                .orElse(null);

                        if (firstSchedule != null) {
                            log.info("Preparing MoveResponse - paths size: {}", paths.size());

                            MoveResponse pathResponse = new MoveResponse(
                                    tripId,
                                    firstSchedule.getId(),
                                    firstSchedule.getPositionPath(),
                                    paths
                            );
                            String pathJsonResponse = objectMapper.writeValueAsString(pathResponse);
                            session.sendMessage(new TextMessage(pathJsonResponse));

                        }
                    }
                } catch (Exception e) {
                    log.error("Error sending path information for tripId: {}", tripId, e);
                }
            }
        });
    }


    // 웹소켓 연결 관리
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // URL에서 tripId 파라미터 추출
        String tripId = extractTripId(session);



        if (tripId != null) {
            // 해당 trip의 세션 목록에 현재 세션 추가
            tripSessions.computeIfAbsent(tripId, k -> ConcurrentHashMap.newKeySet())
                    .add(session);

            // 세션-여행 매핑 정보 저장
            sessionTripMapping.put(session.getId(), tripId);

            log.info("New WebSocket connection established for trip: {}, session: {}",
                    tripId, session.getId());


            try {
                Integer tripIdInt = Integer.valueOf(tripId);
                log.info("About to call handleInitialSync for tripId: {}", tripIdInt);


                handleInitialSync(session, tripIdInt);
                log.info("handleInitialSync completed for tripId: {}", tripIdInt);

            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            } catch (java.io.IOException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                log.error("Error in afterConnectionEstablished for tripId: " + tripId, e);

            }

        }
    }


    //실시간 편집 동기화
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            // ADD타입 따로 처리
            log.info("Received message: {}", message.getPayload());
            JsonNode jsonNode = objectMapper.readTree(message.getPayload());
            if (jsonNode.has("action")) {
                log.info("Action detected, processing ADD request");
                String action = jsonNode.get("action").asText();


                AddRequest addRequest = objectMapper.readValue(message.getPayload(), AddRequest.class);
                Integer tripId = addRequest.getTripId();

                log.info("AddRequest parsed: tripId={}, dayOrder={}", tripId, addRequest.getDayOrder());

                EditRequest.Operation operation = new EditRequest.Operation();
                if ("ADD".equals(action)) {
                    handleAddOperation(tripId, addRequest);
                    operation.setAction("ADD");
                    operation.setScheduleId(scheduleIdCounter.get());
                    operation.setPositionPath(addRequest.getSchedule().getPositionPath());

                } else if ("EDIT".equals(action)) {
                    handleEditOperation(tripId, addRequest);
                    operation.setAction("EDIT");
                    operation.setScheduleId(addRequest.getSchedule().getScheduleId());  // scheduleIdCounter 대신 원래 ID 사용
                    operation.setPositionPath(addRequest.getSchedule().getPositionPath());
                }


                // operation이 초기화된 상태로 EditRequest 생성
                EditRequest editRequest = new EditRequest();  // 기본 생성자 사용
                editRequest.setTripId(tripId);
                editRequest.setOperation(operation);
                editRequest.setOperationId(action);

                // 디버깅을 위한 로그 추가
                log.info("Created EditRequest - tripId: {}, operation action: {}, scheduleId: {}",
                        editRequest.getTripId(),
                        editRequest.getOperation().getAction(),
                        editRequest.getOperation().getScheduleId());



                // 송신자에게 SUCCESS 메시지
                session.sendMessage(new TextMessage("SUCCESS"));

                // EditResponse 생성 및 브로드캐스트
                EditResponse response = EditResponse.createSuccess(editRequest, 1);
                String jsonResponse = objectMapper.writeValueAsString(response);

                String tripIdStr = String.valueOf(tripId);
                Set<WebSocketSession> tripSessionSet = tripSessions.get(tripIdStr);

                if (tripSessionSet != null) {
                    for (WebSocketSession tripSession : tripSessionSet) {
                        if (tripSession.isOpen() && !tripSession.getId().equals(session.getId())) {
                            tripSession.sendMessage(new TextMessage(jsonResponse));
                        }
                    }
                }
                return;

            }


            // 메시지를 EditRequest 객체로 변환
            EditRequest editRequest = objectMapper.readValue(message.getPayload(), EditRequest.class);
            Integer tripId = editRequest.getTripId();
            EditRequest.Operation operation = editRequest.getOperation();

            // tripId null 체크
            if (tripId == null) {
                log.error("Trip ID is null");
                session.sendMessage(new TextMessage("{\"error\": \"Trip ID cannot be null\"}"));
                return;
            }
            log.info("Received message for tripId: {}", tripId);
            log.info("Operation received: {}", operation.getAction());


            stateManager.addEdit(tripId, editRequest);


            switch (operation.getAction()) {
                case "START":
                    // 다른 클라이언트들에게 START 신호를 브로드캐스트
                    String tripIdStr = String.valueOf(tripId);
                    Set<WebSocketSession> tripSessionSet = tripSessions.get(tripIdStr);

                    if (tripSessionSet != null) {
                        for (WebSocketSession tripSession : tripSessionSet) {
                            if (tripSession.isOpen()) {  // 모든 세션에 대해 sync 실행
                                handleInitialSync(tripSession, tripId);
                            }
                        }
                    }//                    handleStartOperation(session, tripId);
                    break;
                case "MOVE":
                    handleMoveOperation(tripId, operation);
                    break;

                case "ADD":
                    handleAddOperation(tripId, objectMapper.convertValue(message.getPayload(), AddRequest.class));
                    break;
                case "DELETE":

                    scheduleCache.removePosition(
                            tripId.toString(),
                            operation.getScheduleId()
                    );

                    // 메모리 상태에서 제거
                    stateManager.removeState(
                            tripId,
                            operation.getScheduleId()
                    );
                    log.info("DELETE SUCCESS");
                    break;

            }


            // 송신자에게 즉시 "SUCCESS" 메시지 전송(테스트용)
            session.sendMessage(new TextMessage("SUCCESS"));

            // EditResponse클래스의 팩토리 메서드 사용(일단 버전 관리는 생략)
            EditResponse response = EditResponse.createSuccess(editRequest, Integer.valueOf(1));
            String jsonResponse = objectMapper.writeValueAsString(response);
            System.out.println("Sending response: " + jsonResponse); // 디버깅용

            // tripId를 String으로 변환하여 tripSessions에서 조회
            String tripIdStr = String.valueOf(tripId);
            Set<WebSocketSession> tripSessionSet = tripSessions.get(tripIdStr);


            // 다른 세션들에게 메시지 브로드캐스트
            if (tripSessionSet != null && !tripSessionSet.isEmpty()) {
                for (WebSocketSession tripSession : tripSessionSet) {
                    if (tripSession.isOpen() && !tripSession.getId().equals(session.getId())) {
                        tripSession.sendMessage(new TextMessage(jsonResponse));
//                    if (tripSession.isOpen()) {
//                        tripSession.sendMessage(new TextMessage(jsonResponse));
                    }
                }
            }

        } catch (IOException e) {
            log.error("Error handling message", e);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 연결 해제 및 감지 처리
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String tripId = sessionTripMapping.remove(session.getId());
        if (tripId != null) {
            Set<WebSocketSession> tripSessionSet = tripSessions.get(tripId);
            if (tripSessionSet != null) {
                // 세션 제거
                tripSessionSet.remove(session);

                // 마지막 사용자가 나갔는지 확인
                if (tripSessionSet.isEmpty()) {
                    tripSessions.remove(tripId);

                    // 이벤트 발행
                    // String -> Integer 변환은 이벤트 발행 직전에만 수행
//                    Integer tripIdInt = Integer.parseInt(tripId);
                    Integer tripIdInt = Integer.valueOf(Integer.parseInt(tripId));
                    eventPublisher.publishEvent(new TripEditFinishEvent(tripIdInt));
//                    eventPublisher.publishEvent(new TripEditFinishEvent(tripId));


                    // TODO: 경로 계산 트리거
                    log.info("Last user disconnected from trip: {}. Triggering route calculation.", tripId);
                }
            }
        }
        log.info("WebSocket connection closed for session: {}", session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket transport error: {}", exception.getMessage(), exception);
    }

    private String extractTripId(WebSocketSession session) {
        String path = session.getUri().getPath();
        String query = session.getUri().getQuery();

        // URL 파라미터에서 tripId 추출 로직
        // 예: ws://domain/ws?tripId=123
        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("tripId=")) {
                    return param.substring(7);
                }
            }
        }
        return null;
    }

    // route 정보 전송용 broadcast
    public void broadcastRouteInfo(String tripId, RouteResponse response) {
        log.info("Broadcasting route info for tripId: {}", tripId);

        try {
            String jsonResponse = objectMapper.writeValueAsString(response);
            Set<WebSocketSession> tripSessionSet = tripSessions.get(tripId);
            if (tripSessionSet != null) {
                for (WebSocketSession session : tripSessionSet) {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(jsonResponse));
                    }
                }
            }
        } catch (IOException | JsonProcessingException e) {
            log.error("Error broadcasting route info", e);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleEditOperation(Integer tripId, AddRequest request) {
        try {
            AddRequest.ScheduleDto scheduleDto = request.getSchedule();
            if (scheduleDto == null) {
                log.error("Schedule information is missing in request");
                throw new IllegalArgumentException("Schedule cannot be null");
            }

            Schedule schedule = scheduleRepository.findById(scheduleDto.getScheduleId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Schedule not found with id: " + scheduleDto.getScheduleId()));

            // duration과 placeName 업데이트
            schedule.setDuration(scheduleDto.getDuration());
            schedule.setPlaceName(scheduleDto.getPlaceName());
            // DB 업데이트
            scheduleRepository.save(schedule);

            // tripEdits에 변경된 Schedule 정보 저장 (기존 로직 유지)
            stateManager.addEditSchedule(tripId, scheduleDto);
            log.info("EDIT operation processed - tripId: {}, scheduleId: {}, new duration: {}, new placeName: {}",
                    tripId,
                    scheduleDto.getScheduleId(),
                    scheduleDto.getDuration(),
                    scheduleDto.getPlaceName());

        } catch (Exception e) {
            log.error("Error handling EDIT operation", e);
            throw new RuntimeException("Failed to process EDIT operation", e);
        }

    }


    // MOVE 비동기처리
    private void handleMoveOperation(Integer tripId, EditRequest.Operation operation) throws JsonProcessingException {
        log.info("=== START handleMoveOperation for tripId: {} ===", tripId);

        try {
            // 1. schedulePositions 초기화 체크
            log.info("Checking schedulePositions initialization for tripId: {}", tripId);
            if (!stateManager.hasPositions(tripId)) {
                List<Schedule> schedules = scheduleRepository.findAllByTripId(tripId);
                stateManager.initializeSchedulePositions(tripId, schedules);
            }

            // 2. position 상태 업데이트
            log.info("Updating state for tripId: {}, scheduleId: {}", tripId, operation.getScheduleId());

            stateManager.updateState(
                    tripId,
                    operation.getScheduleId(),
                    operation.getPositionPath()
            );

            // 3. 현재 상태를 모든 클라이언트에게 전송
            EditRequest.Operation newOperation = new EditRequest.Operation();
            newOperation.setAction("MOVE");  // Action 명시적 설정
            newOperation.setScheduleId(operation.getScheduleId());
            newOperation.setPositionPath(operation.getPositionPath());

            EditRequest newEditRequest = new EditRequest();
            newEditRequest.setTripId(tripId);
            newEditRequest.setOperation(newOperation);

            EditResponse response = EditResponse.createSuccess(newEditRequest, 1);
            broadcastToTripSessions(tripId, objectMapper.writeValueAsString(response));

            // 4. path 생성은 콜백으로 비동기 처리
            stateManager.generatePathsForSchedule(tripId, operation.getScheduleId(), paths -> {
                if (paths != null) {
                    try {
                        MoveResponse pathResponse = new MoveResponse(
                                tripId,
                                operation.getScheduleId(),
                                operation.getPositionPath(),
                                paths
                        );
                        String pathJsonResponse = objectMapper.writeValueAsString(pathResponse);
                        broadcastToTripSessions(tripId, pathJsonResponse);
                    } catch (Exception e) {
                        log.error("Error broadcasting path for tripId: {}", tripId, e);
                    }
                }
            });

            log.info("=== END handleMoveOperation for tripId: {} ===", tripId);

        } catch (Exception e) {
            log.error("Error in handleMoveOperation for tripId: {}", tripId, e);
            // 에러 로깅에 더 자세한 정보 추가
            log.error("Operation details - scheduleId: {}, positionPath: {}",
                    operation.getScheduleId(), operation.getPositionPath());
        }
    }


    private void broadcastToTripSessions(Integer tripId, String message) {
        Set<WebSocketSession> sessions = tripSessions.get(tripId.toString());
        if (sessions != null) {
            sessions.forEach(session -> {
                try {
                    if (session.isOpen()) {
                        try {
                            session.sendMessage(new TextMessage(message));
                        } catch (java.io.IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                } catch (IOException e) {
                    log.error("Error sending message to session for tripId: {}", tripId, e);
                }
            });
        }
    }


    // ADD처리
    private AtomicInteger scheduleIdCounter = new AtomicInteger(16);  // 16부터 시작

    private void handleAddOperation(Integer tripId, AddRequest addRequest) {
        try {
            log.info("Handling ADD operation for tripId: {}", tripId);

//            Integer dayId = addRequest.getDayId();
//            AddRequest.ScheduleDto receivedSchedule = addRequest.getSchedule();
            Integer dayOrder = addRequest.getDayOrder();  // 직접 몇 일차인지 받음
            AddRequest.ScheduleDto receivedSchedule = addRequest.getSchedule();


            // day 번호에 따른 positionPath 범위 계산
            int dayBase = dayOrder * 10000;
            int dayStart = dayBase + 1;
            int dayEnd = dayBase + 9999;

            // stateManager에서 현재 trip의 스케줄 positions 가져오기
            Map<Integer, Integer> currentPositions = stateManager.getSchedulePositions(tripId);

            // 현재 day의 마지막 positionPath 찾기
            Optional<Integer> maxPosition = currentPositions.values().stream()
                    .filter(pos -> pos >= dayStart && pos <= dayEnd)
                    .max(Integer::compareTo);

            // 새로운 positionPath 계산
            int newPosition;
            if (!maxPosition.isPresent()) {
                newPosition = (dayStart + dayEnd) / 2;
            } else {
                newPosition = maxPosition.get() + 10;  // 기존 마지막 일정보다 10 큰 값
            }

            // 해당 trip의 모든 dayId 조회
            List<Integer> dayIds = dayRepository.findIdByTripId(tripId);
            if (dayIds == null || dayIds.isEmpty()) {
                throw new EntityNotFoundException("No days found for trip: " + tripId);
            }
            Integer firstDayId = dayIds.get(0);  // 첫 번째 dayId

            // 조회한 dayIds를 saveDayId에 저장
            saveDayId.put(tripId, dayIds);
            log.info("Saved dayIds for tripId {}: {}", tripId, dayIds);

            // Day와 Trip 엔티티 조회
            Day day = dayRepository.findById(firstDayId)
                    .orElseThrow(() -> new EntityNotFoundException("Day not found: " + firstDayId));
            Trip trip = tripRepository.findById(tripId)
                    .orElseThrow(() -> new EntityNotFoundException("Trip not found: " + tripId));

            // Schedule 엔티티 생성 및 저장
            Schedule schedule = new Schedule(
                    null,  // id는 DB에서 자동 생성
                    day,
                    trip,
                    receivedSchedule.getPlaceName(),
                    1,    // orderNum
                    receivedSchedule.getLat(),
                    receivedSchedule.getLng(),
                    receivedSchedule.getType(),
                    newPosition,
                    receivedSchedule.getDuration()
            );

            Schedule savedSchedule = scheduleRepository.save(schedule);
            Integer newScheduleId = savedSchedule.getId();

            log.info("Saved new schedule to DB: scheduleId={}, position={}", newScheduleId, newPosition);

            // 현재 tripDetail 가져오기
            TripDetailDTO currentTripDetail = stateManager.getTripDetail(tripId);

            // dayId는 1부터 시작하므로 리스트 인덱스는 dayId - 1
            int dayIndex = dayOrder - 1;
            if (dayIndex >= 0 && dayIndex < currentTripDetail.getDayDtos().size()) {
                DayDto targetDay = currentTripDetail.getDayDtos().get(dayIndex);
                List<ScheduleDTO> schedules = targetDay.getSchedules();

                // 새로운 ScheduleDTO 생성
                ScheduleDTO newSchedule = new ScheduleDTO(
                        newScheduleId,
                        receivedSchedule.getPlaceName(),
                        addRequest.getTimeStamp(),
                        newPosition,
                        receivedSchedule.getDuration(),
                        receivedSchedule.getLat(),
                        receivedSchedule.getLng(),
                        receivedSchedule.getType()
                );


                schedules.add(newSchedule);
                schedules.sort(Comparator.comparing(ScheduleDTO::getPositionPath));

                // path 생성 응답 객체
                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("tripId", tripId);
                responseMap.put("scheduleId", newScheduleId);
                responseMap.put("newPosition", newPosition);
                responseMap.put("scheduleDTO", newSchedule);

                // scheduleDTO를 responseMap에서 제거하고 따로 전송
                ScheduleDTO scheduleToSend = (ScheduleDTO) responseMap.remove("scheduleDTO");

                // 이전 schedule과의 path생성
                if (schedules.size() > 1) {
                    Schedule source = scheduleRepository.findById(schedules.get(schedules.size() - 2).getId()).orElse(null);

                    if (source != null) {
                        // 먼저 기본 정보 브로드캐스트
                        String responseMessage = objectMapper.writeValueAsString(responseMap);
                        broadcastToTripSessions(tripId, responseMessage);

                        // scheduleDTO 전송
                        String scheduleMessage = objectMapper.writeValueAsString(scheduleToSend);
                        broadcastToTripSessions(tripId, scheduleMessage);

                        // TripDetail 전송
                        String tripDetailMessage = objectMapper.writeValueAsString(currentTripDetail);
                        broadcastToTripSessions(tripId, tripDetailMessage);

                        // 경로 계산은 콜백으로 처리

                        stateManager.generatePathWithCallback(source, savedSchedule, paths -> {
                            try {
                                if (!paths.isEmpty()) {
                                    Map<String, Object> pathResponse = new HashMap<>();
                                    pathResponse.put("tripId", tripId);
                                    pathResponse.put("paths", paths);

                                    String pathMessage = objectMapper.writeValueAsString(pathResponse);
                                    broadcastToTripSessions(tripId, pathMessage);

                                    log.info("Path info broadcasted for tripId: {}, schedules {} -> {}",
                                            tripId, source.getId(), savedSchedule.getId());
                                }
                            } catch (JsonProcessingException e) {
                                log.error("Error broadcasting path info", e);
                            }
                        });

                        log.info("Path generation requested for tripId: {}", tripId);
                    }



                    }


                }
//                // scheduleDTO를 responseMap에서 제거하고 따로 전송
//                ScheduleDTO scheduleToSend = (ScheduleDTO) responseMap.remove("scheduleDTO");

//                // paths와 position 정보 먼저 전송
//                String responseMessage = objectMapper.writeValueAsString(responseMap);
//                broadcastToTripSessions(tripId, responseMessage);
//
//                // scheduleDTO 따로 전송
//                String scheduleMessage = objectMapper.writeValueAsString(scheduleToSend);
//                broadcastToTripSessions(tripId, scheduleMessage);
//
//                // 업데이트된 TripDetailDTO 브로드캐스트
//                String tripDetailMessage = objectMapper.writeValueAsString(currentTripDetail);
//                broadcastToTripSessions(tripId, tripDetailMessage);

                log.info("Updated TripDetail sent for tripId: {}, dayId: {}, scheduleId: {}",
                        tripId, dayOrder, newScheduleId);
            } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }


    }


}