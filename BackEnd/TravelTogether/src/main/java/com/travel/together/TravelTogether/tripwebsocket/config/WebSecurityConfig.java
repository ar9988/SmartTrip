package com.travel.together.TravelTogether.tripwebsocket.config;

import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@NoArgsConstructor
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())

                .securityMatcher("/ws/**", "/topic/**", "/app/**")  // WebSocket 경로만 처리
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/ws/**", "/topic/**", "/app/**").permitAll()
                );

        return http.build();
    }
}
