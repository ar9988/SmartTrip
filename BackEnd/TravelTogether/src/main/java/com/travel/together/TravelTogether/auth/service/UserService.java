package com.travel.together.TravelTogether.auth.service;

import com.travel.together.TravelTogether.auth.dto.TokenResponseDto;
import com.travel.together.TravelTogether.auth.dto.UserRequestDto;
import com.travel.together.TravelTogether.auth.dto.UserResponseDto;
import com.travel.together.TravelTogether.auth.entity.User;
import com.travel.together.TravelTogether.auth.repository.UserRepository;
import com.travel.together.TravelTogether.auth.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

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
                .password(passwordEncoder.encode(userRequestDto.getPassword()))
                .name(userRequestDto.getName())
                .build();

        userRepository.save(user);
        return userRepository.save(user);
    }

    //Login
    public TokenResponseDto loginUser(String email, String password) {
        //사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        //비밀번호 조회
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        //JWT 토큰 생성
        String token = jwtTokenProvider.generateToken(user.getEmail());

        return TokenResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .profile(user.getProfile())
                .token("Bearer " + token)
                .build();

    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToResponseDto(user);
    }


    private UserResponseDto convertToResponseDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .profile(user.getProfile())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .createdTrips(user.getCreatedTrips().stream()
                        .map(trip -> trip.getTitle()) // Trip의 이름 추출
                        .collect(Collectors.toList()))
                .tripMemberships(user.getTripMemberships().stream()
                        .map(tripMember -> tripMember.getTrip().getTitle()) // Trip 이름 추출
                        .collect(Collectors.toList()))
                .photos(user.getPhotos().stream()
                        .map(photo -> photo.getFilePath()) // Photo URL 추출
                        .collect(Collectors.toList()))
                .build();
    }
}
