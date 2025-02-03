package com.travel.together.TravelTogether.tripwebsocket.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.together.TravelTogether.tripwebsocket.dto.RouteResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class RouteService {
    // 대중교통용
    private final String ODSAY_API_KEY = "GEshN7viFhh9l2tKtWSIdw";
    private final String ODSAY_BASE_URL = "https://api.odsay.com/v1/api/searchPubTransPathT";
    private final WebClient webClient;
    // 개인차량용
    private final String DIRECTION_API_ID = "nom1wg0ckc";
    private final String DIRECTION_API_KEY = "VID442ub9KoZah2ZDf9zltrCxE6oG1aubM24I51T";
    private final String DIRECTION_BASE_URL = "https://naveropenapi.apigw.ntruss.com/map-direction/v1";


    @Value("${odsay.api.key}")
    private String apiKey;

//     두 지점간의 경로정보 계산
    public RouteResponse.Routes calculateRoute(Coordinate start, Coordinate end, Integer dayId) {
        RouteResponse.Routes route = new RouteResponse.Routes();
        route.setDayId(dayId);

        // 대중교통 정보 계산
        RouteResponse.Routes.TransportInfo publicTransport = calculatePublicTransport(start, end);
        // TODO: 자동차 정보 계산
//        RouteResponse.Routes.TransportInfo personalVehicle = calculatePersonalVehicle(start, end);

        route.setPublicTransport(publicTransport);
//        route.setPersonalVehicle(personalVehicle);
        route.setDuration(publicTransport.getDuration()); // 기본 duration은 대중교통 시간으로 설정

        return route;
    }


    // ODsay API 사용해서 대중교통 경로 조회
    private RouteResponse.Routes.TransportInfo calculatePublicTransport(Coordinate start, Coordinate end) {
        try {
            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(ODSAY_BASE_URL)
                            .queryParam("apiKey", apiKey)
                            .queryParam("SX", start.getLongitude())
                            .queryParam("SY", start.getLatitude())
                            .queryParam("EX", end.getLongitude())
                            .queryParam("EY", end.getLatitude())
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // ODsay API 응답 파싱
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            JsonNode result = root.path("result");

            // 경로 정보 추출(path의 0번 인덱스 추출)
            int totalTime = result.path("path").get(0).path("info").path("totalTime").asInt();

            RouteResponse.Routes.TransportInfo transportInfo = new RouteResponse.Routes.TransportInfo();
            transportInfo.setDuration(totalTime);
            transportInfo.setType(1);

            return transportInfo;

        } catch (Exception e) {
            log.error("Failed to calculate public transport route", e);
            // 에러 시 기본값 반환 또는 예외 처리
            return createDefaultTransportInfo();
        }
    }



    // 개인차량 경로 계산 추후 Directions API 구현
    private RouteResponse.Routes.TransportInfo calculatePersonalVehicle(Coordinate start, Coordinate end) {
        // TODO: Directions API
        return createDefaultTransportInfo();
    }


    // 대중교통 객체 생성
    private RouteResponse.Routes.TransportInfo createDefaultTransportInfo() {
        RouteResponse.Routes.TransportInfo defaultInfo = new RouteResponse.Routes.TransportInfo();
        defaultInfo.setDuration(0);
        defaultInfo.setType(1); // 1: 대중교통
        return defaultInfo;
    }
    // 개인차량 객체 생성
    private RouteResponse.Routes.TransportInfo createDefaultPersonalVehicleInfo() {
        RouteResponse.Routes.TransportInfo defaultInfo = new RouteResponse.Routes.TransportInfo();
        defaultInfo.setDuration(0);
        defaultInfo.setType(2);  // 2: 개인차량
        return defaultInfo;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Coordinate {
        private Double latitude;
        private Double longitude;
    }
}
