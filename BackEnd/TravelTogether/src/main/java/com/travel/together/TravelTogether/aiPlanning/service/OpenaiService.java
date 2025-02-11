package com.travel.together.TravelTogether.aiPlanning.service;

import com.travel.together.TravelTogether.aiPlanning.dto.OpenaiRequestDto;
import com.travel.together.TravelTogether.aiPlanning.dto.OpenaiResponseDto;
import com.travel.together.TravelTogether.firebase.service.FCMTokenService;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class OpenaiService {

    private static final Dotenv dotenv = Dotenv.configure()
            .filename("env.properties")
            .load();
    private static final String API_KEY = dotenv.get("OPENAI_API_KEY");
    private static final int MAX_TOKENS = 4096; // 최대 토큰 수 설정

    private final FCMTokenService fcmTokenService;
    private final AiplanningService aiplanningService;

    public OpenaiService(FCMTokenService fcmTokenService, AiplanningService aiplanningService) {
        this.fcmTokenService = fcmTokenService;
        this.aiplanningService = aiplanningService;
    }


    // 프롬프트를 외부 파일에서 읽어오기
    private String loadPromptTemplate() {
//        try {
//            // resources 폴더에서 텍스트 파일 읽기
//            String filePath = ResourceUtils.getFile("classpath:promptTemplate.txt").getAbsolutePath();
//            log.info(filePath);
//            return new String(Files.readAllBytes(Paths.get(filePath)));
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null;
//        }
        try {
            // ClassPathResource를 사용하여 JAR 내부에서도 정상적으로 파일 읽기
            ClassPathResource resource = new ClassPathResource("promptTemplate.txt");
            InputStream inputStream = resource.getInputStream();

            // BufferedReader로 파일 내용 읽기
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                return content.toString();
            }
        } catch (IOException e) {
            log.error("프롬프트 템플릿 파일을 읽는 중 오류 발생", e);
            return null;
        }
    }

    // List<String>을 JSON 배열 형태로 변환하는 메서드
    private String listToJsonArray(List<String> list) {
        if (list != null && !list.isEmpty()) {
            // org.json.JSONArray를 사용하여 List<String>을 JSON 배열 형식으로 변환
            JSONArray jsonArray = new JSONArray(list);
            return jsonArray.toString(); // JSON 배열 문자열 반환
        }
        return "[]"; // 비어있으면 빈 배열 반환
    }


    public OpenaiResponseDto callOpenaiApi(OpenaiRequestDto requestDTO) {
        OpenaiResponseDto openaiResponseDto = null;
//        try {
//            String url = "https://api.openai.com/v1/chat/completions";
//
//            // API 요청 본문 생성
//            JSONObject requestBody = new JSONObject();
//            requestBody.put("model", "gpt-4o-mini");
//            requestBody.put("max_tokens", MAX_TOKENS);
//
//            // 외부 파일에서 프롬프트 템플릿을 로드
//            String promptTemplate = loadPromptTemplate();
//
//            // 요청 데이터로 프롬프트 템플릿을 동적으로 생성
//            String prompt = promptTemplate
//                    .replace("{startDate}", requestDTO.getStartDate())
//                    .replace("{startTime}", requestDTO.getStartTime())
//                    .replace("{endDate}", requestDTO.getEndDate())
//                    .replace("{endTime}", requestDTO.getEndTime())
//                    .replace("{destination}", listToJsonArray(requestDTO.getDestination())) // List<String>을 JSON 배열로 변환
//                    .replace("{places}", listToJsonArray(requestDTO.getPreferences().getPlaces())) // Preferences의 places 변환
//                    .replace("{theme}", listToJsonArray(requestDTO.getPreferences().getTheme())); // Preferences의 theme 변환
//
//
//            JSONArray messages = new JSONArray();
//            JSONObject message = new JSONObject();
//            message.put("role", "user");
//            message.put("content", prompt);
//            messages.put(message);
//
//            requestBody.put("messages", messages);
        try {
            String url = "https://api.openai.com/v1/chat/completions";

            // API 요청 본문 생성
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "gpt-4o-mini");
            requestBody.put("max_tokens", MAX_TOKENS);

            // 외부 파일에서 프롬프트 템플릿을 로드
            String promptTemplate = loadPromptTemplate();
            if (promptTemplate == null) {
                throw new RuntimeException("프롬프트 템플릿을 불러올 수 없습니다.");
            }

            // 요청 데이터로 프롬프트 템플릿을 동적으로 생성
            String prompt = promptTemplate
                    .replace("{startDate}", requestDTO.getStartDate())
                    .replace("{startTime}", requestDTO.getStartTime())
                    .replace("{endDate}", requestDTO.getEndDate())
                    .replace("{endTime}", requestDTO.getEndTime())
                    .replace("{destination}", listToJsonArray(requestDTO.getDestination()))
                    .replace("{places}", listToJsonArray(requestDTO.getPreferences().getPlaces()))
                    .replace("{theme}", listToJsonArray(requestDTO.getPreferences().getTheme()));

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

            System.out.println(promptResponse);
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
                    activity.setType(activityJson.getInt("type"));
                    activity.setPositionPath(activityJson.getInt("positionPath"));
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

            aiplanningService.savePlanningData(openaiResponseDto);
            fcmTokenService.sendNotificationToUser(requestDTO.getUserId(),"일정 생성 완료", "일정 생성이 완료되었습니다.");
            return openaiResponseDto; // API 응답을 반환


        } catch (Exception e) {
            e.printStackTrace();
        }

        // 응답 DTO 반환
        return openaiResponseDto;
    }
}
