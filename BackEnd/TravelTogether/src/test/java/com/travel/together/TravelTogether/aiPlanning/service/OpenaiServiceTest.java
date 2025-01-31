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
        OpenaiRequestDto requestDto = new OpenaiRequestDto("What is the capital of France?");

        // HttpClient를 Mock 객체로 생성
        HttpClient mockHttpClient = Mockito.mock(HttpClient.class);
        OpenaiService openaiService = new OpenaiService();

        // Mocking HttpResponse with a predefined JSON response
        HttpResponse mockResponse = Mockito.mock(HttpResponse.class);
        String mockJsonResponse = "{\"choices\":[{\"message\":{\"content\":\"Paris\"}}]}";
        Mockito.when(mockResponse.getEntity()).thenReturn(new org.apache.http.entity.StringEntity(mockJsonResponse));

        // When: 실제 서비스 호출 (모의된 HttpClient를 사용)
        String responseJson = openaiService.callOpenaiApi(requestDto); // 호출 후 응답은 String
        System.out.println(responseJson);

    }
}
