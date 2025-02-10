package com.travel.together.TravelTogether.tripwebsocket.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Configuration
public class ExecutorConfig {
    @Bean
    public ExecutorService executorService() {
        log.info("Creating ExecutorService bean");  // 로그 추가
        ExecutorService service = Executors.newFixedThreadPool(10);
        log.info("ExecutorService bean created successfully");  // 로그 추가
        return service;
    }
}