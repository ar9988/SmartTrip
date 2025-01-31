package com.travel.together.TravelTogether.aiPlanning.service;

import com.travel.together.TravelTogether.aiPlanning.dto.FestivalRequestDto;
import com.travel.together.TravelTogether.aiPlanning.dto.FestivalResponseDto;
import org.json.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class FestivalService {
    private static final Dotenv dotenv = Dotenv.configure()
            .filename("env.properties")
            .load();
    private static final String SERVICEKEY = dotenv.get("FESTIVAL_SERVICE_KEY");
    private static final String FESTIVAL_URL = "https://apis.data.go.kr/B551011/KorService1/searchFestival1";

    // 축제 데이터 조회
    public FestivalResponseDto getFestivals(FestivalRequestDto requestDto) throws IOException {
        // 축제 열람은 오늘 날짜 기준, 행사의 개수(numOfRows)는 10개로 고정
        String url = FESTIVAL_URL
                + "?serviceKey=" //서비스키
                + SERVICEKEY
                + "&eventStartDate=" //여행 시작 날짜
                + requestDto.getStartDate()
                + "&eventEndDate=" // 여행 종료 날짜
                + requestDto.getEndDate()
                + "&areaCode=" // 지역 코드
                + requestDto.getRegionNumber()
                + "&numOfRows=10&pageNo=1&MobileOS=ETC&MobileApp=AppTest&_type=json&listYN=Y&arrange=A";

        // API 호출
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);
        HttpResponse response = client.execute(request);

        // 응답 데이터 읽기
        String festivalJson = "";
        BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
        festivalJson = br.readLine();

        // JSON 응답을 파싱하여 반환
        JSONObject jsonResponse = new JSONObject(festivalJson);
        JSONObject festivals = jsonResponse.getJSONObject("response")
                .getJSONObject("body")
                .getJSONObject("items");

        return new FestivalResponseDto(festivals);
    }
}
