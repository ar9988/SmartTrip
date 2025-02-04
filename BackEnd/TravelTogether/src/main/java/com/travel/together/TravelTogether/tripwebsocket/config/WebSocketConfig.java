package com.travel.together.TravelTogether.tripwebsocket.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.together.TravelTogether.tripwebsocket.dto.TripEditCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private TripEditCache tripEditCache;

    @Autowired
    private ObjectMapper objectMapper;


    @Bean
    public TripScheduleWebSocketHandler tripScheduleWebSocketHandler() {
        return new TripScheduleWebSocketHandler(
                applicationEventPublisher,
                tripEditCache,
                objectMapper
        );  // 직접 생성
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(tripScheduleWebSocketHandler(), "/ws")
                .setAllowedOriginPatterns("*");

    }

}