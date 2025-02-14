package com.travel.together.TravelTogether.trip.controller;

import com.travel.together.TravelTogether.trip.dto.TripCreateDto;
import com.travel.together.TravelTogether.trip.dto.TripDetailResponse;
import com.travel.together.TravelTogether.trip.dto.TripRequestDto;
import com.travel.together.TravelTogether.trip.dto.TripResponse;
import com.travel.together.TravelTogether.trip.entity.Trip;
import com.travel.together.TravelTogether.trip.service.TripViewService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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

//    @GetMapping("/{userId}/{tripId}")
//    public ResponseEntity<TripCreateDto> createTrip(@RequestBody TripRequestDto requestDto) {
//        TripCreateDto tripCreateDto = tripService.createTrip(requestDto);
//        return ResponseEntity.ok(tripCreateDto);
//    }


    @PostMapping("/")
    public ResponseEntity<TripCreateDto> createTrip(@Valid @RequestBody TripRequestDto tripRequestDto) {
//        tripRequestDto.setUserId(userId);  // TripRequestDto에 setter 추가 필요
        TripCreateDto createdTrip = tripViewService.createTrip(tripRequestDto);
        log.info("Trip Created = {}", createdTrip.getTitle());
        return new ResponseEntity<>(createdTrip, HttpStatus.CREATED);
    }




    @GetMapping("/latest")
    public ResponseEntity<TripResponse> getLatestTrip() {
        TripResponse response = tripViewService.findUpcomingTrip();
        return response != null
                ? ResponseEntity.ok(response)
                : ResponseEntity.notFound().build();
    }


}
