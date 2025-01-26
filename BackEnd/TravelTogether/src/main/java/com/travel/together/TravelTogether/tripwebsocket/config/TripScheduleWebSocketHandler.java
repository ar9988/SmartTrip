package com.travel.together.TravelTogether.tripwebsocket.config;

import com.travel.together.TravelTogether.tripwebsocket.event.TripEditFinishEvent;
import io.jsonwebtoken.io.IOException;
import lombok.extern.slf4j.Slf4j;
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
public class TripScheduleWebSocketHandler extends TextWebSocketHandler {

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
            String tripId = sessionTripMapping.get(session.getId());
            if (tripId == null) {
                return;
            }

            // 같은 trip의 다른 모든 세션에게 메시지 브로드캐스트
            Set<WebSocketSession> tripSessionSet = tripSessions.get(tripId);
            if (tripSessionSet != null) {
                for (WebSocketSession tripSession : tripSessionSet) {
                    if (tripSession.isOpen() && !tripSession.getId().equals(session.getId())) {
                        tripSession.sendMessage(message);
                    }
                }
            }
        } catch (IOException | java.io.IOException e) {
            log.error("Error handling message", e);
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
                    Integer tripIdInt = Integer.parseInt(tripId);
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
}