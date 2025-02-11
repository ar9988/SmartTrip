package com.travel.together.TravelTogether.invitation.service;

import com.travel.together.TravelTogether.auth.entity.User;
import com.travel.together.TravelTogether.auth.repository.UserRepository;
import com.travel.together.TravelTogether.trip.entity.Trip;
import com.travel.together.TravelTogether.trip.entity.TripMember;
import com.travel.together.TravelTogether.trip.repository.TripMemberRepository;
import com.travel.together.TravelTogether.trip.repository.TripRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InviteService {
    private RedisTemplate<String, String> redisTemplate;
    private final TripMemberRepository tripMemberRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    public String generateInviteLink(Integer tripId) {
        // UUID 기반 토큰 생성
        String inviteToken = UUID.randomUUID().toString();

        // Redis에 토큰 저장
        redisTemplate.opsForValue().set(inviteToken, tripId.toString(), Duration.ofDays(1));

        return "http://43.202.51.112:8080/invite?token=" + inviteToken;

    }

    @Transactional
    public void acceptInvite(String token, Long userId) {
        // Redis에서 토큰을 사용한 tripId 조회
        String tripIdStr = redisTemplate.opsForValue().get(token);
        if (tripIdStr == null) {
            throw new IllegalArgumentException("잘못된 초대 링크 입니다.");
        }
        Integer tripId = Integer.parseInt(tripIdStr);

        // 여행, 멤버 조회
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new EntityNotFoundException("여행 일정이 존재하지 않습니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        // 중복 확인
        if (tripMemberRepository.existsByTripIdAndUserId(tripId, userId.intValue())) {
            throw new IllegalArgumentException("이미 초대된 여행 일정 입니다.");
        }

        TripMember tripMember = new TripMember(trip, user);
        tripMemberRepository.save(tripMember);

        //초대 후 토큰 삭제
        redisTemplate.delete(token);

    }
}
