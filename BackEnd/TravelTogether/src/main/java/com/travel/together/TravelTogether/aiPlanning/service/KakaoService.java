package com.travel.together.TravelTogether.aiPlanning.service;

import com.travel.together.TravelTogether.aiPlanning.dto.*;
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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@Service
public class KakaoService {
    private static final Dotenv dotenv = Dotenv.configure()
            .filename("env.properties")
            .load();
    private static final String API_KEY = dotenv.get("KAKAO_API_KEY"); // Kakao REST API Key
    private static final String KEYWORD_URL = "https://dapi.kakao.com/v2/local/search/keyword";

    // 키워드로 장소 검색하는 메서드
    public KakaoResponseDto searchByKeyword(KakaoRequestDto requestDto) throws UnsupportedEncodingException {
        String url = KEYWORD_URL + "?query=" + URLEncoder.encode(requestDto.getKeyword(), "UTF-8");
        JSONObject json = getJson(url);
        JSONArray placeArray = json.getJSONArray("documents");

        List<KakaoDto> placeList = new ArrayList<>();

        // 리스트 길이만큼 반복문 진행
        for (int i = 0; i < placeArray.length(); i++) {
            JSONObject placeObj = placeArray.getJSONObject(i);

            KakaoDto place = new KakaoDto(
                    placeObj.getString("place_name"),
                    placeObj.optString("address_name"),
                    placeObj.optDouble("y"),
                    placeObj.optDouble("x")
            );
            placeList.add(place);
        }
        return new KakaoResponseDto(placeList);
    }

    // Kakao API로부터 JSON 응답을 받아오는 메서드
    private JSONObject getJson(String apiUrl) {
        String json = "";
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet getRequest = new HttpGet(apiUrl);
            getRequest.addHeader("Authorization", "KakaoAK " + API_KEY);
            HttpResponse getResponse = client.execute(getRequest);

            BufferedReader br = new BufferedReader(new InputStreamReader(getResponse.getEntity().getContent(), "UTF-8"));
            json = br.readLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new JSONObject(json);
    }
}
