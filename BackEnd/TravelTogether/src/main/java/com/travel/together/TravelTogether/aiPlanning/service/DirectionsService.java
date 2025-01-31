package com.travel.together.TravelTogether.aiPlanning.service;


import com.travel.together.TravelTogether.aiPlanning.dto.DirectionsRequestDto;
import com.travel.together.TravelTogether.aiPlanning.dto.DirectionsResponseDto;
import io.github.cdimascio.dotenv.Dotenv;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;

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
        Integer totalTime = responseJson.getJSONObject("route")
                .getJSONArray("traoptimal")
                .getJSONObject(0)  // 첫 번째 항목 가져오기
                .getJSONObject("summary")
                .getInt("duration");
        return new DirectionsResponseDto(totalTime);
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
