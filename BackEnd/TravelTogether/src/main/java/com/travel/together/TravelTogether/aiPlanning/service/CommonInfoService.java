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
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class CommonInfoService {

    private static final Dotenv dotenv = Dotenv.configure()
            .filename("env.properties")
            .load();
    private static final String SERVICEKEY = dotenv.get("FESTIVAL_SERVICE_KEY");
    private static final String COMMONINFO_URL = "http://apis.data.go.kr/B551011/KorService1/detailCommon1";

    public CommonInfoResponseDto getCommonInfo(CommonInfoRequestDto requestDto) throws IOException {
        String url = COMMONINFO_URL
                + "?serviceKey=" //서비스키
                + SERVICEKEY
                + "&contentId=" // 콘텐츠 id
                + requestDto.getContentid()
                + "&MobileOS=ETC&MobileApp=AppTest&_type=json&overviewYN=Y&numOfRows=1&pageNo=1";

        // API 호출
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);
        HttpResponse response = client.execute(request);

        BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
        StringBuilder CommonInfoJson = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            CommonInfoJson.append(line);
        }

        // JSON 데이터 파싱
        JSONObject jsonResponse = new JSONObject(CommonInfoJson.toString());
        JSONArray commonInfoArray = jsonResponse.getJSONObject("response")
                .getJSONObject("body")
                .getJSONObject("items")
                .getJSONArray("item");

        JSONObject CommonInfoObj = commonInfoArray.getJSONObject(0);
        String overview = CommonInfoObj.getString("overview");

        return new CommonInfoResponseDto(overview);
    }
}
