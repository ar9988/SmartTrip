package com.travel.together.TravelTogether.aiPlanning.controller;

import com.travel.together.TravelTogether.aiPlanning.dto.KakaoRequestDto;
import com.travel.together.TravelTogether.aiPlanning.dto.KakaoResponseDto;
import com.travel.together.TravelTogether.aiPlanning.dto.KeywordSearchRequestDto;
import com.travel.together.TravelTogether.aiPlanning.dto.KeywordSearchResponseDto;
import com.travel.together.TravelTogether.aiPlanning.service.KakaoService;
import com.travel.together.TravelTogether.aiPlanning.service.KeywordSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/keyword")
public class KeywordSearchController {
    private final KeywordSearchService keywordSearchService;

    @Autowired
    public KeywordSearchController(KeywordSearchService keywordSearchService) {
        this.keywordSearchService = keywordSearchService;
    }

    // 키워드로 장소 검색하는 엔드포인트
    @GetMapping("/search")
    public ResponseEntity<KeywordSearchResponseDto> searchImageUrl(@RequestParam String keyword) {
        try {
            KeywordSearchRequestDto requestDto = new KeywordSearchRequestDto(keyword);
            KeywordSearchResponseDto responseDto = keywordSearchService.getKeywordSearch(requestDto);
            return ResponseEntity.ok(responseDto);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new KeywordSearchResponseDto("Error fetching data"));
        }
    }
}
