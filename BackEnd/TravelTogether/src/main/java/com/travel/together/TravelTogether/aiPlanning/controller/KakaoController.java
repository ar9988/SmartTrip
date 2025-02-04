package com.travel.together.TravelTogether.aiPlanning.controller;

import com.travel.together.TravelTogether.aiPlanning.dto.KakaoRequestDto;
import com.travel.together.TravelTogether.aiPlanning.dto.KakaoResponseDto;
import com.travel.together.TravelTogether.aiPlanning.dto.KeywordSearchRequestDto;
import com.travel.together.TravelTogether.aiPlanning.dto.KeywordSearchResponseDto;
import com.travel.together.TravelTogether.aiPlanning.service.KakaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/kakao")
public class KakaoController {
    private final KakaoService kakaoService;

    @Autowired
    public KakaoController(KakaoService kakaoApiService) {
        this.kakaoService = kakaoApiService;
    }

    // 키워드로 장소 검색하는 엔드포인트
    @GetMapping("/search")
    public ResponseEntity<KakaoResponseDto> searchImageUrl(@RequestParam String keyword) {
        try {
            KakaoRequestDto requestDto = new KakaoRequestDto(keyword);
            KakaoResponseDto responseDto = kakaoService.searchByKeyword(requestDto);
            return ResponseEntity.ok(responseDto);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new KakaoResponseDto("Error fetching data", 0.0, 0.0));
        }
    }

}
