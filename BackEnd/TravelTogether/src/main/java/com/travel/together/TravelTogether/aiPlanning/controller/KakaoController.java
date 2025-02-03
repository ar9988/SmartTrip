package com.travel.together.TravelTogether.aiPlanning.controller;

import com.travel.together.TravelTogether.aiPlanning.dto.KakaoRequestDto;
import com.travel.together.TravelTogether.aiPlanning.dto.KakaoResponseDto;
import com.travel.together.TravelTogether.aiPlanning.service.KakaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kakao")
public class KakaoController {
    private final KakaoService kakaoService;

    @Autowired
    public KakaoController(KakaoService kakaoApiService) {
        this.kakaoService = kakaoApiService;
    }

    // 키워드로 장소 검색하는 엔드포인트
    @PostMapping("/search")
    public KakaoResponseDto searchPlace(@RequestBody KakaoRequestDto requestDto) {
        try {
            return kakaoService.searchByKeyword(requestDto);
        } catch (Exception e) {
            e.printStackTrace();
            // 에러 처리 (적절한 에러 메시지를 반환할 수 있습니다)
            return new KakaoResponseDto("Error",0.0,0.0);
        }
    }
}
