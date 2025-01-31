package com.travel.together.TravelTogether.aiPlanning.controller;

import com.travel.together.TravelTogether.aiPlanning.dto.OdsayRequestDto;
import com.travel.together.TravelTogether.aiPlanning.dto.OdsayResponseDto;
import com.travel.together.TravelTogether.aiPlanning.service.OdsayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/odsay")
public class OdsayController {
    private final OdsayService odsayService;

    public OdsayController(OdsayService odsayService) {
        this.odsayService = odsayService;
    }

    @PostMapping("/path")
    public ResponseEntity<OdsayResponseDto> getPublicTransportPath(@RequestBody OdsayRequestDto requestDto) {
        OdsayResponseDto responseDto = odsayService.getPublicTransportPath(requestDto);
        return ResponseEntity.ok(responseDto);
    }
}
