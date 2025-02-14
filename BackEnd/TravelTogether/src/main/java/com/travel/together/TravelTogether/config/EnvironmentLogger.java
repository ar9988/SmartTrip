package com.travel.together.TravelTogether.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.Map;

@Slf4j
@Component
public class EnvironmentLogger implements CommandLineRunner {

    private final Environment env;

    public EnvironmentLogger(Environment env) {
        this.env = env;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Active profiles: {}", Arrays.toString(env.getActiveProfiles()));
        log.info("Server port: {}", env.getProperty("server.port"));
        log.info("Datasource URL: {}", env.getProperty("spring.datasource.url"));
        log.info("Java version: {}", System.getProperty("java.version"));
        log.info("OS: {}", System.getProperty("os.name"));
    }
}
