package com.travel.together.TravelTogether.trip.controller;

import com.travel.together.TravelTogether.auth.entity.User;
import com.travel.together.TravelTogether.auth.jwt.JwtTokenProvider;
import com.travel.together.TravelTogether.auth.repository.UserRepository;
import com.travel.together.TravelTogether.trip.dto.TripCreateDto;
import com.travel.together.TravelTogether.trip.dto.TripRequestDto;
import com.travel.together.TravelTogether.trip.dto.TripResponse;
import com.travel.together.TravelTogether.trip.dto.TripUpdateRequest;
import com.travel.together.TravelTogether.trip.service.TripViewService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/trips")
@Tag(name = "Trips API", description = "여행계획조회 API")    // swagger UI 설정
public class TripViewController {
    public TripViewController(TripViewService tripViewService, JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        this.tripViewService = tripViewService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }
    private final TripViewService tripViewService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
//     일정 전체조회
    @GetMapping("/{userId}")
    public ResponseEntity<TripResponse> getAllTrip(
            @PathVariable Integer userId) {
        TripResponse response = tripViewService.getAllTrip(userId);
        log.info("trip GET success");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/")
    public boolean createTrip(@Valid @RequestBody TripRequestDto tripRequestDto) {
//        tripRequestDto.setUserId(userId);  // TripRequestDto에 setter 추가 필요
        TripCreateDto createdTrip = tripViewService.createTrip(tripRequestDto);
        log.info("Trip Created = {}", createdTrip.getTitle());

        // True/False로 응답 수정
//        return new ResponseEntity<>(createdTrip, HttpStatus.CREATED);
        return true;
    }


    @DeleteMapping("/{userId}/{tripId}")
    public boolean deleteTrip(@PathVariable Integer tripId) {
        tripViewService.deleteTrip(tripId);
//        return ResponseEntity.noContent().build();
        log.info("DELETE SUCCESS");
        return true;
    }

    @PutMapping("{userId}/{tripId}")
    public boolean updateTrip(@PathVariable Integer tripId, @RequestBody @Valid TripUpdateRequest request) {

        tripViewService.updateTrip(tripId, request);
        return true;
    }


    @GetMapping("/latest")
    public ResponseEntity<TripResponse> getLatestTrip(@RequestHeader("Authorization") String token) {
        // JWT 토큰에서 사용자 정보 추출
        String jwtToken = token.replace("Bearer", "").trim();
        String userEmail = jwtTokenProvider.getEmailFromToken(jwtToken);

        // 사용자 조회
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 해당 사용자의 최근 여행 조회
        TripResponse response = tripViewService.findUpcomingTrip(user.getId());
        return response != null
                ? ResponseEntity.ok(response)
                : ResponseEntity.notFound().build();
    }


}
