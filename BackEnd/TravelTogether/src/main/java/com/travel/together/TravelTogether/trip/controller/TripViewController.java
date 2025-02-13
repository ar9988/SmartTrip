package com.travel.together.TravelTogether.trip.controller;

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
    public TripViewController(TripViewService tripViewService) {
        this.tripViewService = tripViewService;
    }
    private final TripViewService tripViewService;

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



}
