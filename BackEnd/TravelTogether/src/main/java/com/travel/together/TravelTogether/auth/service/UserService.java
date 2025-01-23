package com.travel.together.TravelTogether.auth.service;

import com.travel.together.TravelTogether.auth.dto.UserRequestDto;
import com.travel.together.TravelTogether.auth.entity.User;
import com.travel.together.TravelTogether.auth.repository.UserRepository;
import com.travel.together.TravelTogether.auth.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    //회원가입
    public User registerUser(UserRequestDto userRequestDto){
        //이메일 중복 확인
        if (userRepository.findByEmail(userRequestDto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }

        //user 저장
        User user = User.builder()
                .email(userRequestDto.getEmail())
                .passwordHash(passwordEncoder.encode(userRequestDto.getPasswordHash()))
                .nickname(userRequestDto.getNickname())
                .profile(userRequestDto.getProfile())
                .build();

        userRepository.save(user);
        return userRepository.save(user);
    }

    //Login
    public String loginUser(String email, String password) {
        //사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        //비밀번호 조회
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("invalid credentials");
        }

        //JWT 토큰 생성
        return jwtTokenProvider.generateToken(user.getEmail());

    }
}
