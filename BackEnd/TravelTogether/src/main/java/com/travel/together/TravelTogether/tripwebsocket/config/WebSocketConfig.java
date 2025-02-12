package com.travel.together.TravelTogether.tripwebsocket.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.together.TravelTogether.trip.repository.DayRepository;
import com.travel.together.TravelTogether.trip.repository.ScheduleRepository;
import com.travel.together.TravelTogether.trip.repository.TripRepository;
import com.travel.together.TravelTogether.trip.service.TripService;
import com.travel.together.TravelTogether.tripwebsocket.cache.TripEditCache;
import com.travel.together.TravelTogether.tripwebsocket.cache.TripScheduleCache;
import com.travel.together.TravelTogether.tripwebsocket.service.ScheduleService;
import com.travel.together.TravelTogether.tripwebsocket.service.TripStateManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final TripEditCache tripEditCache;
    private final ObjectMapper objectMapper;
    private final ScheduleService scheduleService;
    private final TripScheduleCache tripScheduleCache;
    private final TripStateManager tripStateManager;
    private final TripService tripService;
    private final ScheduleRepository scheduleRepository;
    private final ExecutorService executorService;
    private final DayRepository dayRepository;
    private final TripRepository tripRepository;
    @Autowired
    public WebSocketConfig(
            ApplicationEventPublisher applicationEventPublisher,
            TripEditCache tripEditCache,
            ObjectMapper objectMapper,
            ScheduleService scheduleService,
            TripScheduleCache tripScheduleCache,
            TripStateManager tripStateManager,
            TripService tripService,
            ScheduleRepository scheduleRepository,
            ExecutorService executorService, DayRepository dayRepository, TripRepository tripRepository) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.tripEditCache = tripEditCache;
        this.objectMapper = objectMapper;
        this.scheduleService = scheduleService;
        this.tripScheduleCache = tripScheduleCache;
        this.tripStateManager = tripStateManager;
        this.tripService = tripService;
        this.scheduleRepository = scheduleRepository;
        this.executorService = executorService;
        this.dayRepository = dayRepository;
        this.tripRepository = tripRepository;
    }

    @Bean
    public TripScheduleWebSocketHandler tripScheduleWebSocketHandler() {
        return new TripScheduleWebSocketHandler(
                applicationEventPublisher,
                tripEditCache,
                objectMapper,
                scheduleService,
                tripScheduleCache,
                tripStateManager,
                tripService,
                scheduleRepository,
                executorService,
                dayRepository,
                tripRepository

        );  // 직접 생성
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(tripScheduleWebSocketHandler(), "/ws")
                .setAllowedOriginPatterns("*");

    }

}