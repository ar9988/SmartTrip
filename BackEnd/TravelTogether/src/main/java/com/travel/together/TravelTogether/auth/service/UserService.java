package com.travel.together.TravelTogether.auth.service;

import com.travel.together.TravelTogether.album.service.S3Service;
import com.travel.together.TravelTogether.auth.dto.ProfileUpdateRequestDto;
import com.travel.together.TravelTogether.auth.dto.TokenResponseDto;
import com.travel.together.TravelTogether.auth.dto.UserRequestDto;
import com.travel.together.TravelTogether.auth.dto.UserResponseDto;
import com.travel.together.TravelTogether.auth.entity.User;
import com.travel.together.TravelTogether.auth.repository.UserRepository;
import com.travel.together.TravelTogether.auth.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final S3Service s3Service;

    //회원가입
    public User registerUser(UserRequestDto userRequestDto, MultipartFile profile_image){
        String s3Url = null;
        if (profile_image != null && !profile_image.isEmpty()) {
            byte[] fileBytes;
            try {
                fileBytes = profile_image.getBytes();
            } catch (IOException e) {
                throw new RuntimeException("파일 바이트 읽기 실패", e);
            }

            try (ByteArrayInputStream uploadStream = new ByteArrayInputStream(fileBytes)) {
                s3Url = s3Service.uploadFile(uploadStream, profile_image.getSize(),
                        profile_image.getContentType(),
                        profile_image.getOriginalFilename());
            } catch (IOException e) {
                throw new RuntimeException("S3 업로드 중 에러 발생", e);
            }
        }
        //이메일 중복 확인
        if (userRepository.findByEmail(userRequestDto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }



        //user 저장
        User user = User.builder()
                .email(userRequestDto.getEmail())
                .password(passwordEncoder.encode(userRequestDto.getPassword()))
                .name(userRequestDto.getName())
                .profile_image(s3Url)
                .profile(userRequestDto.getProfile())
                .build();

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
                .profile_image(user.getProfile_image())
                .profile(user.getProfile())
                .token("Bearer " + token)
                .build();

    }

    public User profileUpdate(
            String token,
            ProfileUpdateRequestDto profileUpdateRequestDto,
            MultipartFile profileImage){

        String userEmail = jwtTokenProvider.getEmailFromToken(token);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (profileUpdateRequestDto.getName() != null && !profileUpdateRequestDto.getName().isEmpty()){
            user.setName(profileUpdateRequestDto.getName());
        }

        if (profileUpdateRequestDto.getProfile() != null && !profileUpdateRequestDto.getProfile().isEmpty()){
            user.setProfile(profileUpdateRequestDto.getProfile());
        }

        if (profileImage != null && !profileImage.isEmpty()){

            if (user.getProfile_image() != null) {
                s3Service.deleteFile(user.getProfile_image());
            }

            try (ByteArrayInputStream uploadStream = new ByteArrayInputStream(profileImage.getBytes())) {
                String s3Url = s3Service.uploadFile(uploadStream, profileImage.getSize(),
                        profileImage.getContentType(), profileImage.getOriginalFilename());
                user.setProfile_image(s3Url);
            } catch (IOException e) {
                throw new RuntimeException("S3 업로드 중 에러 발생", e);
            }
        }
        return userRepository.save(user);
    }


    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

//    public UserResponseDto getUserById(Long id) {
//        User user = userRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//        return convertToResponseDto(user);
//    }
//
//
//    private UserResponseDto convertToResponseDto(User user) {
//        return UserResponseDto.builder()
//                .id(user.getId())
//                .email(user.getEmail())
//                .name(user.getName())
//                .profile_image(user.getProfile_image())
//                .profile(user.getProfile())
//                .createdAt(user.getCreatedAt())
//                .updatedAt(user.getUpdatedAt())
//                .createdTrips(user.getCreatedTrips().stream()
//                        .map(trip -> trip.getTitle()) // Trip의 이름 추출
//                        .collect(Collectors.toList()))
//                .tripMemberships(user.getTripMemberships().stream()
//                        .map(tripMember -> tripMember.getTrip().getTitle()) // Trip 이름 추출
//                        .collect(Collectors.toList()))
//                .photos(user.getPhotos().stream()
//                        .map(photo -> photo.getFilePath()) // Photo URL 추출
//                        .collect(Collectors.toList()))
//                .build();
//    }
}
