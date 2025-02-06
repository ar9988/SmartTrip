package com.travel.together.TravelTogether.tripwebsocket.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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


    // tripId를 키로 하고, 해당 여행의 접속자들의 세션을 값으로 가지는 Map
    private final Map<String, Set<WebSocketSession>> tripSessions = new ConcurrentHashMap<>();

    // 세션 ID를 키로 하고, tripId를 값으로 가지는 Map (세션이 어느 여행에 속해있는지 추적)
    // "session123" -> "trip456", 하나의 trip에 여러 session이 매핑됨
    private final Map<String, String> sessionTripMapping = new ConcurrentHashMap<>();


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




            // 모든 작업 캐시에 저장
//            editCache.addEdit(tripId, editRequest);
            editCache.addEdit(tripId.toString(), editRequest);


            switch (operation.getAction()) {
                case "START":
                    try {
                        log.info("GET START");
                        TripDetailDTO tripDetail = tripService.getTripDetailById(tripId);
                        TripDetailResponse response = new TripDetailResponse(
                                tripId,
                                tripDetail.getTitle(),
                                tripDetail.getMembers(),
                                tripDetail.getDayDtos(),
                                tripDetail.getCreatedAt(),
                                tripDetail.getUpdatedAt()
                        );
                        String jsonResponse = objectMapper.writeValueAsString(response);
                        session.sendMessage(new TextMessage(jsonResponse));
                    } catch (Exception e) {
                        log.error("Error processing START action", e);
                        session.sendMessage(new TextMessage("{\"error\": \"Failed to process START action\"}"));
                    }
                    break;
                case "MOVE":
                    // 작업 내용 저장
                    stateManager.addEdit(tripId, editRequest);
                    // position 업데이트
                    stateManager.updateState(
                            tripId,
                            operation.getScheduleId(),
                            operation.getPositionPath()
                    );
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

}