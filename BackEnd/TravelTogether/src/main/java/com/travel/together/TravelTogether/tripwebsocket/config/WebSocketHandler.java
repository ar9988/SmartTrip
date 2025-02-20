package com.travel.together.TravelTogether.tripwebsocket.config;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

    // 방 별로 그림 데이터 저장 (방 ID -> 그림 데이터 리스트)
//    private final Map<String, List<String>> roomDrawings = new ConcurrentHashMap<>();

    // 현재 접속 중인 클라이언트 세션 (세션 ID -> 세션 객체)
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        System.out.println("사용자 연결됨: " + session.getId());
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session.getId());
        System.out.println("사용자 연결 종료: " + session.getId() + " 상태: " + status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("WebSocket 오류 발생: " + exception.getMessage());
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
        sessions.remove(session.getId());
    }

}