package com.travel.together.TravelTogether.auth.controller;

import com.travel.together.TravelTogether.auth.dto.*;
import com.travel.together.TravelTogether.auth.entity.User;
import com.travel.together.TravelTogether.auth.jwt.JwtTokenProvider;
import com.travel.together.TravelTogether.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    //회원가입 처리
    @PostMapping("/signup")
    public ResponseEntity<UserResponseDto> registerUser(
            @RequestPart(value = "signup_data") UserRequestDto userRequestDto,
            @RequestPart(value = "profile_image", required = false) MultipartFile profile_image) {
        User user = userService.registerUser(userRequestDto, profile_image);

        UserResponseDto responseDto = UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .profile_image(user.getProfile_image())
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

    //Profile 수정
    @PatchMapping("/profile")
    public ResponseEntity<UserResponseDto> profileUpdate(
            @RequestHeader("Authorization") String token,
            @RequestPart(value = "profile_data", required = false)ProfileUpdateRequestDto profileUpdateRequestDto,
            @RequestPart(value = "profile_image", required = false) MultipartFile profileImage) {

        String jwtToken = token.replace("Bearer", "").trim();
        log.info(jwtToken);

        User updatedUser = userService.profileUpdate(jwtToken, profileUpdateRequestDto, profileImage);

        UserResponseDto responseDto = UserResponseDto.builder()
                .id(updatedUser.getId())
                .email(updatedUser.getEmail())
                .name(updatedUser.getName())
                .profile_image(updatedUser.getProfile_image())
                .profile(updatedUser.getProfile())
                .createdAt(updatedUser.getCreatedAt())
                .updatedAt(updatedUser.getUpdatedAt())
                .build();
        return ResponseEntity.ok(responseDto);
    }

}
