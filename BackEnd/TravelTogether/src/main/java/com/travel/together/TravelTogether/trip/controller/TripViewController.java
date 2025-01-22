package com.travel.together.TravelTogether.trip.controller;

import com.travel.together.TravelTogether.trip.dto.TripDetailResponse;
import com.travel.together.TravelTogether.trip.dto.TripScheduleResponse;
import com.travel.together.TravelTogether.trip.service.TripViewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripViewController {
    public TripViewController(TripViewService tripViewService) {
        this.tripViewService = tripViewService;
    }

    private final TripViewService tripViewService;

    // 일정 전체조회
    @GetMapping("/{tripId}")
    public ResponseEntity<TripScheduleResponse> getAllSchedules(
            @PathVariable Long tripId) {
        TripScheduleResponse response = tripViewService.getAllSchedules(tripId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/{tripId}")
    public ResponseEntity<TripDetailResponse> getTripDetail(
            @PathVariable Long userId,
            @PathVariable Long tripId) {
        TripDetailResponse response = tripViewService.getTripDetail(userId, tripId);
        return ResponseEntity.ok(response);
    }
}
