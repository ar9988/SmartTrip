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
import java.util.ArrayList;
import java.util.List;

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
                    +
                    "출력 데이터 형식\n" +
                    "{\n" +
                    "  \"title\": \"string\",        \n" +
                    "  \"start_date\": \"string\",   \n" +
                    "  \"end_date\": \"string\", \n" +
                    "  \"destination\": [\"string\"],\n" +
                    "  \"schedule\": {\n" +
                    "    \"days\": [\n" +
                    "      {\n" +
                    "        \"date\": \"string\",\n" +
                    "        \"activities\": [\n" +
                    "          {\n" +
                    "            \"name\": \"string\",\n" +
                    "            \"duration\": number\n" +
                    "          }\n" +
                    "        ]\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  }\n" +
                    "}\n" +
                    // 프롬프트 조건 작성
                    "일정 생성 규칙은 아래와 같다.\n" +
                    "1. 응답 형식\n" +
                    "- JSON 형식으로만 응답\n" +
                    "- 부가 설명 없이 JSON 데이터만 제공\n" +
                    "- start_date, end_date는 \"YYYYMMDD\" 형식\n" +
                    "- date 는 \"YYYY-MM-DD\" 형식\n" +
                    "- name은 구체적인 장소명으로 작성 (예: \"북한산국립공원\", \"국립중앙박물관\")\n" +
                    "- duration은 분 단위로 설정한다.\n";

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

            // 답변의 마크업, 공백 제거
            promptResponse = promptResponse
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .replaceAll("  ","")
                    .replaceAll("\\\\","")
                    .replaceAll("\n","")
                    .trim();

            // DTO 생성
            JSONObject promptJson = new JSONObject(promptResponse);

            // OpenaiResponseDto로 변환
            openaiResponseDto = new OpenaiResponseDto();
            openaiResponseDto.setTitle(promptJson.getString("title"));
            openaiResponseDto.setStartDate(promptJson.getString("start_date"));
            openaiResponseDto.setEndDate(promptJson.getString("end_date"));

            // destination 파싱
            List<String> destinations = new ArrayList<>();
            JSONArray destinationJsonArray = promptJson.getJSONArray("destination");
            for (int i = 0; i < destinationJsonArray.length(); i++) {
                destinations.add(destinationJsonArray.getString(i));
            }
            openaiResponseDto.setDestination(destinations);

            // schedule 파싱
            OpenaiResponseDto.DaySchedule schedule = new OpenaiResponseDto.DaySchedule();
            List<OpenaiResponseDto.DateActivities> dateActivitiesList = new ArrayList<>();

            JSONArray daysJsonArray = promptJson.getJSONObject("schedule").getJSONArray("days");
            for (int i = 0; i < daysJsonArray.length(); i++) {
                JSONObject dayJson = daysJsonArray.getJSONObject(i);

                // DateActivities 객체 생성
                OpenaiResponseDto.DateActivities dateActivities = new OpenaiResponseDto.DateActivities();
                dateActivities.setDate(dayJson.getString("date"));

                // activities 파싱
                List<OpenaiResponseDto.Activity> activities = new ArrayList<>();
                JSONArray activitiesJsonArray = dayJson.getJSONArray("activities");
                for (int j = 0; j < activitiesJsonArray.length(); j++) {
                    JSONObject activityJson = activitiesJsonArray.getJSONObject(j);
                    OpenaiResponseDto.Activity activity = new OpenaiResponseDto.Activity();
                    activity.setName(activityJson.getString("name"));
                    activity.setDuration(activityJson.getInt("duration"));
                    activities.add(activity);
                }

                // 활동 리스트를 DateActivities에 설정
                dateActivities.setActivities(activities);
                dateActivitiesList.add(dateActivities);
            }

            // DaySchedule에 DateActivities 목록 설정
            schedule.setDays(dateActivitiesList);

            // 전체 DTO에 schedule 설정
            openaiResponseDto.setSchedule(schedule);


        } catch (Exception e) {
            e.printStackTrace();
        }

        // 응답 DTO 반환
        return openaiResponseDto;
    }
}
