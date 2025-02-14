package com.travel.together.TravelTogether.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.Map;

@Component
public class EnvironmentLogger implements CommandLineRunner {

    private final Environment env;

    public EnvironmentLogger(Environment env) {
        this.env = env;
    }

    @Override
    public void run(String... args) throws Exception {
        // 활성화된 프로파일 출력
        System.out.println("Active profiles: " + Arrays.toString(env.getActiveProfiles()));

        // 특정 환경 설정 값 확인 (예: 서버 포트, DB URL 등)
        System.out.println("Server port: " + env.getProperty("server.port"));
        System.out.println("Datasource URL: " + env.getProperty("spring.datasource.url"));

        // 시스템 프로퍼티 출력
        System.out.println("Java version: " + System.getProperty("java.version"));
        System.out.println("OS: " + System.getProperty("os.name"));

        // 모든 환경변수 출력 (주의: 민감한 정보가 포함될 수 있으므로 개발 단계에서만 사용)
        for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }
}
