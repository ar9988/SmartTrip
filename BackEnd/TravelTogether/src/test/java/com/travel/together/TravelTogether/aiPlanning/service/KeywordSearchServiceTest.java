package com.travel.together.TravelTogether.aiPlanning.service;

import static org.junit.jupiter.api.Assertions.*;

import com.travel.together.TravelTogether.aiPlanning.dto.KeywordSearchRequestDto;
import com.travel.together.TravelTogether.aiPlanning.dto.KeywordSearchResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class KeywordSearchServiceTest {

    @Autowired
    private KeywordSearchService keywordSearchService;

    private KeywordSearchRequestDto requestDto;

    @BeforeEach
    void setUp() {
        requestDto = new KeywordSearchRequestDto("festival");
    }

    @Test
    void getKeywordSearch_shouldReturnImageUrl() throws IOException {
        // 실제 API 호출 실행
        KeywordSearchResponseDto responseDto = keywordSearchService.getKeywordSearch(requestDto);
        System.out.println(responseDto.getImageurl());

        // 응답이 null이 아닌지 확인
        assertNotNull(responseDto);
    }
}
