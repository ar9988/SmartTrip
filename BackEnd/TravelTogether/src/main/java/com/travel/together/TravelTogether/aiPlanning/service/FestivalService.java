package com.travel.together.TravelTogether.aiPlanning.service;

import com.travel.together.TravelTogether.aiPlanning.dto.FestivalDto;
import com.travel.together.TravelTogether.aiPlanning.dto.FestivalRequestDto;
import com.travel.together.TravelTogether.aiPlanning.dto.FestivalResponseDto;
import jakarta.validation.constraints.Null;
import org.json.JSONArray;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
                + "&numOfRows=10&pageNo=1&MobileOS=ETC&MobileApp=AppTest&_type=json&listYN=Y&arrange=A";

        // 지역 코드가 존재한다면 url에 추가
        if (requestDto.getRegionNumber() != null) {
            url += "&areaCode=" + requestDto.getRegionNumber();
        }

        // API 호출
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);
        HttpResponse response = client.execute(request);

        BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
        StringBuilder festivalJson = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            festivalJson.append(line);
        }

        // JSON 데이터 파싱
        JSONObject jsonResponse = new JSONObject(festivalJson.toString());
        JSONArray festivalArray = jsonResponse.getJSONObject("response")
                .getJSONObject("body")
                .getJSONObject("items")
                .getJSONArray("item");

        List<FestivalDto> festivalList = new ArrayList<>();

        // 리스트 길이만큼 반복문 진행
        for (int i = 0; i < festivalArray.length(); i++) {
            JSONObject festivalObj = festivalArray.getJSONObject(i);

            FestivalDto festival = new FestivalDto(
                    festivalObj.getString("title"),
                    festivalObj.optString("addr1", ""),
                    festivalObj.optString("firstimage", ""),
                    festivalObj.optString("eventstartdate", ""),
                    festivalObj.optString("eventenddate", ""),
                    festivalObj.optString("contentid", "")
            );

            festivalList.add(festival);
        }

        return new FestivalResponseDto(festivalList);
    }
}
