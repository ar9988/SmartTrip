package com.travel.together.TravelTogether.aiPlanning.controller;

import com.travel.together.TravelTogether.aiPlanning.dto.FestivalDto;
import com.travel.together.TravelTogether.aiPlanning.dto.FestivalRequestDto;
import com.travel.together.TravelTogether.aiPlanning.dto.FestivalResponseDto;
import com.travel.together.TravelTogether.aiPlanning.dto.KakaoRequestDto;
import com.travel.together.TravelTogether.aiPlanning.service.FestivalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
public class FestivalController {
    private final FestivalService festivalService;

    @Autowired
    public FestivalController(FestivalService festivalService) {
        this.festivalService = festivalService;
    }

    @GetMapping("/api/festivals")
    public ResponseEntity<FestivalResponseDto> getFestivals(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam String regionCode) {
        try {
            // 요청 DTO 생성
            FestivalRequestDto requestDto = new FestivalRequestDto(startDate, endDate, regionCode);

            // 서비스 호출 및 응답 반환
            FestivalResponseDto responseDto = festivalService.getFestivals(requestDto);
            return ResponseEntity.ok(responseDto);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new FestivalResponseDto(null));
        }
    }
}
