package com.travel.together.TravelTogether.auth.controller;

import com.travel.together.TravelTogether.auth.dto.UserRequestDto;
import com.travel.together.TravelTogether.auth.dto.LoginRequestDto;
import com.travel.together.TravelTogether.auth.dto.TokenResponseDto;
import com.travel.together.TravelTogether.auth.dto.UserResponseDto;
import com.travel.together.TravelTogether.auth.entity.User;
import com.travel.together.TravelTogether.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    //회원가입 처리
    @PostMapping("/signup")
    public ResponseEntity<UserResponseDto> registerUser(@RequestBody UserRequestDto userRequestDto) {
        User user = userService.registerUser(userRequestDto);
        UserResponseDto responseDto = UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .profile(user.getProfile())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();

        return ResponseEntity.ok(responseDto);
    }

    //Login
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> loginUser(@RequestBody LoginRequestDto loginRequestDto) {
        TokenResponseDto responseDto = userService.loginUser(loginRequestDto.getEmail(), loginRequestDto.getPassword());
        return ResponseEntity.ok(responseDto);
    }

}
