package com.travel.together.TravelTogether.aiPlanning.controller;

import com.travel.together.TravelTogether.aiPlanning.dto.OpenaiRequestDto;
import com.travel.together.TravelTogether.aiPlanning.service.OpenaiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/openai")
public class OpenaiController {

    private final OpenaiService openaiService;

    @Autowired
    public OpenaiController(OpenaiService openaiService) {
        this.openaiService = openaiService;
    }

    // POST 요청을 통해 prompt를 받아 OpenAI API 호출
    @PostMapping("/generate")
    public String generateResponse(@RequestBody OpenaiRequestDto requestDTO) {
        String response = openaiService.callOpenaiApi(requestDTO);
        openaiService.parseAndPrintResponse(response);
        return response; // 응답을 클라이언트에게 전달
    }
}
