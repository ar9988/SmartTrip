package com.travel.together.TravelTogether.invitation.controller;

import com.travel.together.TravelTogether.auth.entity.User;
import com.travel.together.TravelTogether.auth.jwt.JwtTokenProvider;
import com.travel.together.TravelTogether.auth.repository.UserRepository;
import com.travel.together.TravelTogether.auth.service.UserService;
import com.travel.together.TravelTogether.invitation.service.InviteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/invite")
@RequiredArgsConstructor
public class InviteController {
    private final InviteService inviteService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    
    // 초대 링크 생성
    @PostMapping("/{tripId}")
    public ResponseEntity<String> createInviteLink(@PathVariable Integer tripId) {
        String inviteLink = inviteService.generateInviteLink(tripId);
        return ResponseEntity.ok(inviteLink);
    }
    
    // 여행 멤버 추가
    @PostMapping("/accept")
    public ResponseEntity<String> acceptInvite(
            @RequestParam String token,
            @RequestHeader("Authorization") String jwtToken
    ) {
        String jsonToken = jwtToken.replace("Bearer", "").trim();
        String userEmail = jwtTokenProvider.getEmailFromToken(jsonToken);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        inviteService.acceptInvite(token, user.getUserId().longValue());
        return ResponseEntity.ok("여행 멤버에 추가되었습니다.");
    }
}
