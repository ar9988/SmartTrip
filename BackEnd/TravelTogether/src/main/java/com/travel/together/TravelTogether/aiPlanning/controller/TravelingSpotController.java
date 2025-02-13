package com.travel.together.TravelTogether.aiPlanning.controller;

import com.travel.together.TravelTogether.aiPlanning.dto.TravelingSpotDto;
import com.travel.together.TravelTogether.aiPlanning.dto.TravelingSpotRegionDto;
import com.travel.together.TravelTogether.aiPlanning.entity.Favorite;
import com.travel.together.TravelTogether.aiPlanning.entity.TravelingSpot;
import com.travel.together.TravelTogether.aiPlanning.service.TravelingSpotService;
import com.travel.together.TravelTogether.auth.entity.User;
import com.travel.together.TravelTogether.auth.jwt.JwtTokenProvider;
import com.travel.together.TravelTogether.auth.repository.UserRepository;
import jakarta.persistence.Entity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favorite")
public class TravelingSpotController {
    private final TravelingSpotService travelingSpotService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public TravelingSpotController(
            TravelingSpotService travelingSpotService,
            JwtTokenProvider jwtTokenProvider,
            UserRepository userRepository) {
        this.travelingSpotService = travelingSpotService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @GetMapping("/{regionNumber}")
    public ResponseEntity<List<TravelingSpotRegionDto>> getRegionSpot(@PathVariable Integer regionNumber) {
        List<TravelingSpotRegionDto> spots = travelingSpotService.getRegionSpots(regionNumber);
        return ResponseEntity.ok(spots);
    }

    @GetMapping
    public ResponseEntity<List<TravelingSpotDto>> getUserFavorites(@RequestHeader("Authorization") String jwt) {
        String jwtToken = jwt.replace("Bearer", "").trim();
        String userEmail = jwtTokenProvider.getEmailFromToken(jwtToken);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        Integer userId = user.getUserId();
        List<TravelingSpotDto> favorites = travelingSpotService.getUserFavoriteSpot(userId);
        return ResponseEntity.ok(favorites);
    }

    @PostMapping("/{contentId}")
    public ResponseEntity<Boolean> updateFavoriteSpot(
            @RequestHeader("Authorization") String jwt,
            @PathVariable Integer contentId){
        String jwtToken = jwt.replace("Bearer", "").trim();
        String userEmail = jwtTokenProvider.getEmailFromToken(jwtToken);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        Integer userId = user.getUserId();
        Boolean isCompleted = travelingSpotService.updateFavoriteSpot(userId, contentId);
        return ResponseEntity.ok(isCompleted);
    }
}
