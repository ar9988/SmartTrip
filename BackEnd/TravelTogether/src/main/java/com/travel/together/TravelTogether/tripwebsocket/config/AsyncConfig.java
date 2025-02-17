package com.travel.together.TravelTogether.tripwebsocket.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);    // 기본적으로 유지되는 스레드 수
        executor.setMaxPoolSize(30); // 최대 스레드 수
        executor.setQueueCapacity(70);  // 작업 대기열 최대 용량
        executor.setThreadNamePrefix("TripEditAsync-"); //생성되는 스레드 이름 접두사
        executor.initialize(); // 설정 완료 후 executor 초기화
        return executor;
    }

    // void 예외처리
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }
}
