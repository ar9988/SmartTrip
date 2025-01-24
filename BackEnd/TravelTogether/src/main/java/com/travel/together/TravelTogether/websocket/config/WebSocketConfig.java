package com.travel.together.TravelTogether.websocket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 구독용 prefix 설정. 클라이언트가 구독할 때 사용
        config.enableSimpleBroker("/topic");

        // 메시지 발행용 prefix 설정. 클라이언트가 메시지 보낼 때 사용
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 연결 엔드포인트 설정
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:[*]")  // 실제 운영 환경에서는 구체적인 도메인 설정 필요
                .withSockJS();  // SockJS 지원 추가
    }
}