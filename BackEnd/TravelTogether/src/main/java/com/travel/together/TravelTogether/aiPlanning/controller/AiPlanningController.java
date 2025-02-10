package com.travel.together.TravelTogether.aiPlanning.controller;

import com.travel.together.TravelTogether.aiPlanning.dto.*;
import com.travel.together.TravelTogether.aiPlanning.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping
public class AiPlanningController {
    private final OpenaiService openaiService;
    private final DirectionsService directionsService;
    private final OdsayService odsayService;
    private final KakaoService kakaoService;
    private final KeywordSearchService keywordSearchService;

    @Autowired
    public AiPlanningController(OpenaiService openaiService, DirectionsService directionsService, OdsayService odsayService, KakaoService kakaoService, KeywordSearchService keywordSearchService) {
        this.openaiService = openaiService;
        this.directionsService = directionsService;
        this.odsayService = odsayService;
        this.kakaoService = kakaoService;
        this.keywordSearchService = keywordSearchService;
    }

    // POST 요청을 통해 prompt를 받아 OpenAI API 호출
    @PostMapping("/ai/generate")
    public OpenaiResponseDto callOpenaiApi(@RequestBody OpenaiRequestDto requestDTO) {
        return openaiService.callOpenaiApi(requestDTO); // 응답을 클라이언트에게 전달
    }

    // 네이버 directions API 호출
    @PostMapping("/path/directions")
    public DirectionsResponseDto getDrivingDirections(@RequestBody DirectionsRequestDto directionsRequestDto) {
        return directionsService.getDrivingDirections(directionsRequestDto);
    }

    // Odsay API 호출
    @PostMapping("/path/odsay")
    public ResponseEntity<OdsayResponseDto> getPublicTransportPath(@RequestBody OdsayRequestDto requestDto) {
        OdsayResponseDto responseDto = odsayService.getPublicTransportPath(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    // 키워드 검색, 카카오
    @GetMapping("/search/kakao")
    public ResponseEntity<KakaoResponseDto> getInfo(@RequestParam String keyword) {
        try {
            KakaoRequestDto requestDto = new KakaoRequestDto(keyword);
            KakaoResponseDto responseDto = kakaoService.searchByKeyword(requestDto);
            return ResponseEntity.ok(responseDto);
        } catch (Exception  e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new KakaoResponseDto(null));
        }
    }

    // 키워드 검색, 공공 데이터
    @GetMapping("/search/publicdata")
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
