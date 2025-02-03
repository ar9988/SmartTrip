package com.travel.together.TravelTogether.trip.controller;

import com.travel.together.TravelTogether.trip.dto.TripDetailResponse;
import com.travel.together.TravelTogether.trip.dto.TripResponse;
import com.travel.together.TravelTogether.trip.service.TripViewService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/trips")
@Tag(name = "Trips API", description = "여행계획조회 API")    // swagger UI 설정
public class TripViewController {
    public TripViewController(TripViewService tripViewService) {
        this.tripViewService = tripViewService;
    }
    private final TripViewService tripViewService;

//     일정 전체조회
    @GetMapping("/{userId}")
    public ResponseEntity<TripResponse> getAllTrip(
            @PathVariable Integer userId) {
        TripResponse response = tripViewService.getAllTrip(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/{tripId}")
    public ResponseEntity<TripDetailResponse> getTripDetail(
            @PathVariable Integer userId,
            @PathVariable Integer tripId) {
        TripDetailResponse response = tripViewService.getTripDetail(userId, tripId);
        return ResponseEntity.ok(response);
    }
}
