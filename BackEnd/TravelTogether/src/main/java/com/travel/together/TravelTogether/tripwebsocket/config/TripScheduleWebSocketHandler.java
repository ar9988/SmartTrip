package com.travel.together.TravelTogether.tripwebsocket.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.together.TravelTogether.trip.dto.ScheduleDto;
import com.travel.together.TravelTogether.trip.entity.Schedule;
import com.travel.together.TravelTogether.trip.repository.ScheduleRepository;
import com.travel.together.TravelTogether.trip.service.TripService;
import com.travel.together.TravelTogether.tripwebsocket.cache.TripScheduleCache;
import com.travel.together.TravelTogether.tripwebsocket.dto.*;
import com.travel.together.TravelTogether.tripwebsocket.cache.TripEditCache;
import com.travel.together.TravelTogether.tripwebsocket.event.TripEditFinishEvent;
import com.travel.together.TravelTogether.tripwebsocket.service.ScheduleService;
import com.travel.together.TravelTogether.tripwebsocket.service.TripStateManager;
import io.jsonwebtoken.io.IOException;
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



    // tripId를 키로 하고, 해당 여행의 접속자들의 세션을 값으로 가지는 Map
    private final Map<String, Set<WebSocketSession>> tripSessions = new ConcurrentHashMap<>();

    // 세션 ID를 키로 하고, tripId를 값으로 가지는 Map (세션이 어느 여행에 속해있는지 추적)
    // "session123" -> "trip456", 하나의 trip에 여러 session이 매핑됨
    private final Map<String, String> sessionTripMapping = new ConcurrentHashMap<>();





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

        // 2. 현재의 position 정보 적용
        // position 정보 가져오기
        log.info("Getting schedule positions for tripId: {}", tripId);
        Map<Integer, Integer> currentPositions = stateManager.getSchedulePositions(tripId);
        log.info("Retrieved positions. Size: {}", currentPositions != null ? currentPositions.size() : 0);
        if (currentPositions != null) {
            for (DayDto day : currentTripDetail.getDayDtos()) {
                for (ScheduleDTO schedule : day.getSchedules()) {
                    Integer position = currentPositions.get(schedule.getId());
                    log.info("Schedule ID: {}, Current Position: {}", schedule.getId(), position);

                    if (position != null) {
                        schedule.setPositionPath(position);
                    }
                }
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
            }catch (Exception e) {
                log.error("Error in afterConnectionEstablished for tripId: " + tripId, e);

            }
        }
    }







    //실시간 편집 동기화
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            log.info("Received message: {}", message.getPayload());

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
                    handleStartOperation(session, tripId);
                    break;
                case "MOVE":
                    handleMoveOperation(tripId, operation);
                break;

                case "ADD":
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


    private String createErrorResponse(Throwable e) {
        try {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Path calculation failed: " + e.getMessage());
            return objectMapper.writeValueAsString(errorResponse);
        } catch (JsonProcessingException ex) {
            return "{\"error\": \"Internal server error\"}";
        }
    }




}