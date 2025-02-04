package com.travel.together.TravelTogether.aiPlanning.controller;

import com.travel.together.TravelTogether.aiPlanning.dto.CommonInfoRequestDto;
import com.travel.together.TravelTogether.aiPlanning.dto.CommonInfoResponseDto;
import com.travel.together.TravelTogether.aiPlanning.dto.FestivalRequestDto;
import com.travel.together.TravelTogether.aiPlanning.dto.FestivalResponseDto;
import com.travel.together.TravelTogether.aiPlanning.service.CommonInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("api/commoninfo")
public class CommonInfoController {
    private final CommonInfoService commonInfoService;

    @Autowired
    public CommonInfoController(CommonInfoService commonInfoService){
        this.commonInfoService = commonInfoService;
    }

    @GetMapping("/api/festivals")
    public ResponseEntity<CommonInfoResponseDto> getCommonInfo(
            @RequestParam String contentid) {
        try {
            // 요청 DTO 생성
            CommonInfoRequestDto requestDto = new CommonInfoRequestDto(contentid);

            // 서비스 호출 및 응답 반환
            CommonInfoResponseDto responseDto = commonInfoService.getCommonInfo(requestDto);
            return ResponseEntity.ok(responseDto);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new CommonInfoResponseDto(null));
        }
    }
}
