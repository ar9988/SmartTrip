package com.travel.together.TravelTogether.aiPlanning.service;


import com.travel.together.TravelTogether.aiPlanning.dto.DirectionsRequestDto;
import com.travel.together.TravelTogether.aiPlanning.dto.DirectionsResponseDto;
import io.github.cdimascio.dotenv.Dotenv;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class DirectionsService {

    private static final Dotenv dotenv = Dotenv.configure()
            .filename("env.properties")
            .load();
    private static final String BASE_URL = "https://naveropenapi.apigw.ntruss.com/map-direction/v1";
    private final String API_KEY_ID = dotenv.get("DIRECTIONS_API_KEY_ID");
    private final String API_KEY = dotenv.get("DIRECTIONS_API_KEY");

    public DirectionsResponseDto getDrivingDirections(DirectionsRequestDto directionsRequestDto) {
        String url = BASE_URL + "/driving?" +
                "start=" + directionsRequestDto.getStartLongitude() + "," + directionsRequestDto.getStartLatitude() +
                "&goal=" + directionsRequestDto.getEndLongitude() + "," + directionsRequestDto.getEndLatitude();

        JSONObject responseJson = sendRequest(url);

        // 좌표 배열 생성
        JSONArray pathArray = responseJson.getJSONObject("route")
                .getJSONArray("traoptimal")
                .getJSONObject(0)
                .getJSONArray("path");

        List<DirectionsResponseDto.PathPoint> path = new ArrayList<>();

        for (int i = 0; i < pathArray.length(); i++) {
            JSONArray pointArray = pathArray.getJSONArray(i);
            Double longitude = pointArray.getDouble(0);
            Double latitude = pointArray.getDouble(1);
            path.add(new DirectionsResponseDto.PathPoint(latitude, longitude));
        }

        // 총 소요시간
        Integer totalTime = responseJson.getJSONObject("route")
                .getJSONArray("traoptimal")
                .getJSONObject(0)  // 첫 번째 항목 가져오기
                .getJSONObject("summary")
                .getInt("duration")
                /60000; // 밀리초 -> 분 단위 변환

        // DirectionPath 객체 생성 및 데이터 설정
        DirectionsResponseDto.DirectionPath directionPath = new DirectionsResponseDto.DirectionPath();
        directionPath.setPath(path);

        // DTO 객체 생성 및 값 설정
        DirectionsResponseDto responseDto = new DirectionsResponseDto();
        responseDto.setTotalTime(totalTime);
        responseDto.setDirectionPath(directionPath); // 변경된 변수명 사용

        return responseDto;
    }

    private JSONObject sendRequest(String apiUrl) {
        String responseJson = "";
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(apiUrl);

            // 헤더 설정
            request.addHeader("x-ncp-apigw-api-key-id", API_KEY_ID);
            request.addHeader("x-ncp-apigw-api-key", API_KEY);

            // 요청 실행 및 응답 처리
            HttpResponse response = client.execute(request);
            BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            responseJson = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new JSONObject(responseJson);
    }
}
