package com.travel.together.TravelTogether.aiPlanning.service;

import com.travel.together.TravelTogether.aiPlanning.dto.OpenaiRequestDto;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.apache.http.client.HttpClient;
import org.apache.http.HttpResponse;

class OpenaiServiceTest {

    @Test
    void testGenerateResponse() throws Exception {
        // Given: OpenaiRequestDto 객체 준비
        OpenaiRequestDto requestDto = new OpenaiRequestDto("프롬프트 잘 쓰는 법 있나");

        OpenaiService openaiService = new OpenaiService();

        // When: 실제 서비스 호출 (모의된 HttpClient를 사용)
        System.out.println(openaiService.callOpenaiApi(requestDto).getResponseMessage()); // 호출 후 응답은 String
    }
}
