package com.travel.together.TravelTogether.aiPlanning.service;

import com.travel.together.TravelTogether.aiPlanning.dto.OpenaiRequestDto;
import com.travel.together.TravelTogether.aiPlanning.dto.OpenaiResponseDto;
import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class OpenaiService {

    private static final Dotenv dotenv = Dotenv.configure()
            .filename("env.properties")
            .load();
    private static final String API_KEY = dotenv.get("OPENAI_API_KEY");
    private static final int MAX_TOKENS = 4096; // 최대 토큰 수 설정

    public OpenaiResponseDto callOpenaiApi(OpenaiRequestDto requestDTO) {
        OpenaiResponseDto openaiResponseDto = null;
        try {
            String url = "https://api.openai.com/v1/chat/completions";

            // API 요청 본문 생성
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "gpt-4o-mini");
            requestBody.put("max_tokens", MAX_TOKENS);

            // 프롬프트에 공백을 제거.
            String prompt = ""
                    // 입력값 프롬프트
                    + "startDate:" + requestDTO.getStartDate()
                    + "startTime:" + requestDTO.getStartTime()
                    + "endDate:" + requestDTO.getEndDate()
                    + "endTime:" + requestDTO.getEndTime()
                    + "destination:" +  requestDTO.getDestination()
                    + "places:" + requestDTO.getPreferences().getPlaces()
                    + "theme:" + requestDTO.getPreferences().getTheme()
                    // 프롬프트 조건
                    ;

            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", prompt);
            messages.put(message);

            requestBody.put("messages", messages);

            // HTTP 연결 설정
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // 요청 본문 전송
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // 응답 받기
            StringBuilder responseBuilder = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    responseBuilder.append(responseLine.trim());
                }
            }

            // 응답 JSON 파싱
            String response = responseBuilder.toString();
            JSONObject responseJson = new JSONObject(response);

            // 'choices' 배열에서 첫 번째 항목을 가져와서 메시지를 추출
            String promptResponse = responseJson
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

            // 마크업, 공백 제거
            promptResponse = promptResponse
//                    .replaceAll("```json", "")
//                    .replaceAll("```", "")
//                    .replaceAll("","")
                    .replaceAll("\\\\","")
                    .trim();

            // DTO 생성
            openaiResponseDto = new OpenaiResponseDto(promptResponse);

        } catch (Exception e) {
            e.printStackTrace();
            // 예외 발생 시 빈 DTO를 반환하도록 처리
            openaiResponseDto = new OpenaiResponseDto("{}");
        }

        // 응답 DTO 반환
        return openaiResponseDto;
    }
}
