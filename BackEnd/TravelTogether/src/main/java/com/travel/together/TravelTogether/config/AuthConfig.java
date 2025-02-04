package com.travel.together.TravelTogether.config;

import com.travel.together.TravelTogether.auth.jwt.JwtAuthenticationFilter;
import com.travel.together.TravelTogether.auth.jwt.JwtTokenProvider;
import com.travel.together.TravelTogether.auth.helper.UserHelper;
import com.travel.together.TravelTogether.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class AuthConfig {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserHelper userHelper;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, userHelper);
    }

//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(csrf -> csrf.disable())
//                .authorizeHttpRequests(authorize -> authorize
//                        .requestMatchers("/api/auth/**").permitAll()
//                        .requestMatchers("/ws/**", "/topic/**", "/app/**").permitAll()  // WebSocket 경로
//                        .requestMatchers("/api/auth/**").permitAll()  // 인증 관련 경로
//                      .requestMatchers("/swagger-ui.html").permitAll()
//                        .anyRequest().authenticated()
//                )
//                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
