package com.travel.together.TravelTogether.invitation.controller;

import com.travel.together.TravelTogether.auth.entity.User;
import com.travel.together.TravelTogether.auth.jwt.JwtTokenProvider;
import com.travel.together.TravelTogether.auth.repository.UserRepository;
import com.travel.together.TravelTogether.auth.service.UserService;
import com.travel.together.TravelTogether.invitation.dto.InviteAcceptResponseDto;
import com.travel.together.TravelTogether.invitation.dto.InviteRequestDto;
import com.travel.together.TravelTogether.invitation.dto.InviteResponseDto;
import com.travel.together.TravelTogether.invitation.service.InviteService;
import com.travel.together.TravelTogether.trip.entity.Trip;
import com.travel.together.TravelTogether.trip.entity.TripMember;
import com.travel.together.TravelTogether.trip.repository.TripMemberRepository;
import com.travel.together.TravelTogether.trip.repository.TripRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/invite")
@RequiredArgsConstructor
public class InviteController {
    private final InviteService inviteService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final TripMemberRepository tripMemberRepository;
    private final RedisTemplate<String, String> redisTemplate;


    // 초대 링크 생성
    @PostMapping("/{tripId}")
    public ResponseEntity<InviteResponseDto> createInviteLink(
            @PathVariable Integer tripId,
            @RequestHeader("Authorization") String token) {

        String jwtToken = token.replace("Bearer", "").trim();
        String userEmail = jwtTokenProvider.getEmailFromToken(jwtToken);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        List<TripMember> tripMembers = tripMemberRepository.findByTripId(tripId);
        Optional<TripMember> tripMemberOptional = tripMembers.stream()
                .filter(tm -> tm.getUser().getUserId().equals(user.getId()))
                .findFirst();

//        if (tripMemberOptional.isEmpty() || tripMemberOptional.get().getIsOwner() == false) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                    .body("초대 권한이 없습니다.");
//        }

        String inviteToken = inviteService.generateInviteLink(tripId);
        InviteResponseDto responseDto = InviteResponseDto.builder().token(inviteToken).build();
        return ResponseEntity.ok(responseDto);
    }
    
    // 여행 멤버 추가
    @PostMapping("/accept")
    public ResponseEntity<InviteAcceptResponseDto> acceptInvite(
            @RequestBody InviteRequestDto inviteRequestDto,
            @RequestHeader("Authorization") String jwtToken
    ) {
        String jsonToken = jwtToken.replace("Bearer", "").trim();
        String userEmail = jwtTokenProvider.getEmailFromToken(jsonToken);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        String token = inviteRequestDto.getToken();

        InviteAcceptResponseDto responseDto = inviteService.acceptInvite(token, user.getUserId().longValue());
        return ResponseEntity.ok(responseDto);
    }
}
