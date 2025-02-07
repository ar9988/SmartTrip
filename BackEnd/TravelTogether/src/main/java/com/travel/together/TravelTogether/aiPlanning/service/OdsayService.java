package com.travel.together.TravelTogether.aiPlanning.service;

import com.travel.together.TravelTogether.aiPlanning.dto.OdsayRequestDto;
import com.travel.together.TravelTogether.aiPlanning.dto.OdsayResponseDto;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Service;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

@Service
public class OdsayService {
    private static final Dotenv dotenv = Dotenv.configure()
            .filename("env.properties")
            .load();
    private static final String API_KEY = dotenv.get("ODSAY_API_KEY");

    public OdsayResponseDto getPublicTransportPath(OdsayRequestDto requestDto) {
        try {
            String urlInfo = "https://api.odsay.com/v1/api/searchPubTransPathT?" +
                    "SX=" + requestDto.getStartLongitude() +
                    "&SY=" + requestDto.getStartLatitude() +
                    "&EX=" + requestDto.getEndLongitude() +
                    "&EY=" + requestDto.getEndLatitude() +
                    "&apiKey=" + URLEncoder.encode(API_KEY, "UTF-8");

            // HTTP 연결
            URL url = new URL(urlInfo);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-type", "application/json");

            BufferedReader bufferedReader =
                    new BufferedReader(new InputStreamReader(conn.getInputStream()));

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            bufferedReader.close();
            conn.disconnect();

            // JSON 응답 파싱, 최단 경로(0번 인덱스)의 시간 조회
            JSONObject jsonObject = new JSONObject(sb.toString());
            JSONArray pathArray = jsonObject.getJSONObject("result").getJSONArray("path");
            JSONObject firstPath = pathArray.getJSONObject(0);
            Integer totalTime = firstPath.getJSONObject("info").getInt("totalTime");

            // DTO 변환 후 반환(시간 저장)
            return new OdsayResponseDto(totalTime);
        } catch (Exception e) {
            e.printStackTrace();
            return new OdsayResponseDto(0); // 오류 발생 시 0 반환
        }
    }
}

