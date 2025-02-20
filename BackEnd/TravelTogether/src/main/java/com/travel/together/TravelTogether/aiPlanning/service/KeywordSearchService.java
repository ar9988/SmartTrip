package com.travel.together.TravelTogether.aiPlanning.service;

import com.travel.together.TravelTogether.aiPlanning.dto.CommonInfoRequestDto;
import com.travel.together.TravelTogether.aiPlanning.dto.CommonInfoResponseDto;
import com.travel.together.TravelTogether.aiPlanning.dto.KeywordSearchRequestDto;
import com.travel.together.TravelTogether.aiPlanning.dto.KeywordSearchResponseDto;
import io.github.cdimascio.dotenv.Dotenv;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;

@Service
public class KeywordSearchService {
    private static final Dotenv dotenv = Dotenv.configure()
            .filename("env.properties")
            .load();
    private static final String SERVICEKEY = dotenv.get("FESTIVAL_SERVICE_KEY");
    private static final String COMMONINFO_URL = "https://apis.data.go.kr/B551011/KorService1/searchKeyword1";

    public KeywordSearchResponseDto getKeywordSearch(KeywordSearchRequestDto requestDto) throws IOException {
        String url = COMMONINFO_URL
                + "?serviceKey=" //서비스키
                + SERVICEKEY
                + "&keyword=" // 콘텐츠 id
                + URLEncoder.encode(requestDto.getKeyword(), "UTF-8")
                + "&numOfRows=1&pageNo=1&MobileOS=ETC&MobileApp=AppTest&_type=json&listYN=Y&arrange=O";

        // API 호출
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);
        HttpResponse response = client.execute(request);

        BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
        StringBuilder KeywordSearchJson = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            KeywordSearchJson.append(line);
        }

        // JSON 데이터 파싱
        JSONObject jsonResponse = new JSONObject(KeywordSearchJson.toString());
        JSONArray keywordSearchArray = jsonResponse.getJSONObject("response")
                .getJSONObject("body")
                .getJSONObject("items")
                .getJSONArray("item");

        JSONObject KeywordSearchObj = keywordSearchArray.getJSONObject(0);
        String imageurl = KeywordSearchObj.getString("firstimage");

        return new KeywordSearchResponseDto(imageurl);
    }
}
