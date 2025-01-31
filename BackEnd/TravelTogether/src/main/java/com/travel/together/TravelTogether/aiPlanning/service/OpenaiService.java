package com.travel.together.TravelTogether.aiPlanning.service;

import com.travel.together.TravelTogether.aiPlanning.dto.OpenaiRequestDto;
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
    private static final int BUFFER_SIZE = 4096;
    private static final int MAX_TOKENS = 4096; // 최대 토큰 수 설정

    public String callOpenaiApi(OpenaiRequestDto requestDTO) {
        String response = "";
        try {
            String url = "https://api.openai.com/v1/chat/completions";

            // API 요청 본문 생성
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "gpt-4o-mini");
            requestBody.put("max_tokens", MAX_TOKENS);

            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", requestDTO.getPrompt());
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

            response = responseBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public void parseAndPrintResponse(String responseBody) {
        JSONObject jsonObject = new JSONObject(responseBody);
        JSONArray choices = jsonObject.getJSONArray("choices");

        for (int i = 0; i < choices.length(); i++) {
            String text = choices.getJSONObject(i).getJSONObject("message").getString("content");
            printInChunks(text, BUFFER_SIZE);
        }
    }

    private void printInChunks(String text, int bufferSize) {
        int length = text.length();
        int start = 0;

        while (start < length) {
            int end = Math.min(length, start + bufferSize);
            System.out.println(text.substring(start, end));
            start = end;
        }
    }
}
