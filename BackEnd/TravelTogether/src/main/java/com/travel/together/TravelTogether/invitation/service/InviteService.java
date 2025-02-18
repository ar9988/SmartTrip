package com.travel.together.TravelTogether.invitation.service;

import com.travel.together.TravelTogether.auth.entity.User;
import com.travel.together.TravelTogether.auth.repository.UserRepository;
import com.travel.together.TravelTogether.invitation.dto.InviteAcceptResponseDto;
import com.travel.together.TravelTogether.invitation.dto.InviteResponseDto;
import com.travel.together.TravelTogether.trip.entity.Trip;
import com.travel.together.TravelTogether.trip.entity.TripMember;
import com.travel.together.TravelTogether.trip.repository.TripMemberRepository;
import com.travel.together.TravelTogether.trip.repository.TripRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InviteService {
    private final RedisTemplate<String, String> redisTemplate;
    private final TripMemberRepository tripMemberRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    public String generateInviteLink(Integer tripId) {
        // UUID 기반 토큰 생성
        String inviteToken = UUID.randomUUID().toString();

        // Redis에 토큰 저장
        redisTemplate.opsForValue().set(inviteToken, tripId.toString(), Duration.ofDays(1));
        String storedTripId = redisTemplate.opsForValue().get(inviteToken);
        Long ttl = redisTemplate.getExpire(inviteToken);
        return inviteToken;

    }

    @Transactional
    public InviteAcceptResponseDto acceptInvite(String token, Long userId) {
        // Redis에서 초대 토큰 조회
        String tripIdStr = redisTemplate.opsForValue().get(token);
        if (tripIdStr == null) {
            return InviteAcceptResponseDto.builder()
                    .message("잘못된 초대 링크 입니다.")
                    .tripId(-1)
                    .build();
        }

        Integer tripId = Integer.parseInt(tripIdStr);

        // 여행 및 사용자 조회
        Optional<Trip> tripOptional = tripRepository.findById(tripId);

        if (!tripOptional.isPresent()) {
            return InviteAcceptResponseDto.builder()
                    .message("여행 일정이 존재하지 않습니다.")
                    .tripId(tripId)
                    .build();
        }

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new EntityNotFoundException("여행 일정이 존재하지 않습니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        // 이미 초대된 경우
        if (tripMemberRepository.existsByTripIdAndUserId(tripId, userId.intValue())) {
            return InviteAcceptResponseDto.builder()
                    .message("이미 초대된 여행 일정 입니다.")
                    .tripId(tripId)
                    .build();
        }

        // 새로운 멤버 추가
        TripMember tripMember = new TripMember(trip, user);
        tripMemberRepository.save(tripMember);

        return InviteAcceptResponseDto.builder()
                .message("여행 멤버 추가 완료!")
                .tripId(tripId)
                .build();
    }

}
