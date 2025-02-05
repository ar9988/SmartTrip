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
                    + "일정 생성 규칙\n" +
                    "1. 여행 순서\n" +
                    "- destination 배열의 인덱스 순서대로 관광 진행\n" +
                    "\n" +
                    "2. 식사 일정\n" +
                    "- 점심 식사, 저녁 식사만 포함\n" +
                    "- 각 식사는 90분 소요\n" +
                    "- 식사의 name은 \"식사\"로 통일\n" +
                    "\n" +
                    "3. 관광지 추천 우선순위\n" +
                    "- 1순위: preferences.activities ('축제'가 존재할 경우 축제를 1순위로 적용)\n" +
                    "- 2순위: preferences.interests (배열 인덱스 순서대로 우선순위 적용)\n" +
                    "- name은 구체적인 장소명으로 작성 (예: \"북한산국립공원\", \"국립중앙박물관\")\n" +
                    "\n" +
                    "4. 도시 간 이동\n" +
                    "- 다른 도시로 이동 시 \"이동\" 일정 추가\n" +
                    "- 방문했던 도시는 다시 방문하지 않는다.\n" +
                    "\n" +
                    "5. 응답 형식\n" +
                    "- JSON 형식으로만 응답\n" +
                    "- 부가 설명 없이 JSON 데이터만 제공\n" +
                    "\n" +
                    "6. 관광 일정\n" +
                    "- 점심 식사 전의 duration 합은 200 이하로 설정\n" +
                    "- 점심 식사 이후 저녁 식사 전까지 duration의 합은 240\n" +
                    "- 저녁 식사 이후 일정의 duration의 합은 120 이하\n" +
                    "\n" +
                    "7. 축제 일정\n" +
                    "- 일정에 축제가 포함된다면 축제의 duration은 120으로 고정.\n" +
                    "\n" ;

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
