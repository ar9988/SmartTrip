package com.travel.together.TravelTogether.tripwebsocket.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.together.TravelTogether.aiPlanning.dto.DirectionsRequestDto;
import com.travel.together.TravelTogether.aiPlanning.dto.DirectionsResponseDto;
import com.travel.together.TravelTogether.aiPlanning.dto.OdsayRequestDto;
import com.travel.together.TravelTogether.aiPlanning.dto.OdsayResponseDto;
import com.travel.together.TravelTogether.aiPlanning.service.DirectionsService;
import com.travel.together.TravelTogether.aiPlanning.service.OdsayService;
import com.travel.together.TravelTogether.tripwebsocket.dto.RouteResponse;
import io.github.cdimascio.dotenv.Dotenv;
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
    private final WebClient webClient;
    private final OdsayService odsayService;
    private final DirectionsService directionsService;



    //     두 지점간의 경로정보 계산
    public RouteResponse.Routes calculateRoute(Coordinate start, Coordinate end, Integer dayId) {
        RouteResponse.Routes route;
        route = new RouteResponse.Routes();
        route.setDayId(dayId);

        // 대중교통 정보 계산
        OdsayRequestDto odsayRequest = new OdsayRequestDto(
                start.getLongitude(),
                start.getLatitude(),
                end.getLongitude(),
                end.getLatitude()
        );

        OdsayResponseDto odsayResponse = odsayService.getPublicTransportPath(odsayRequest);
        log.info("ODsay Response received in RouteService - totalTime: {}", odsayResponse.getTotalTime());


        log.info("odsayyyyyy",String.valueOf(odsayResponse));
        RouteResponse.Routes.TransportInfo publicTransport;
        publicTransport = new RouteResponse.Routes.TransportInfo();
        publicTransport.setDuration(odsayResponse.getTotalTime());
        log.info("Public Transport Info created - duration: {}", publicTransport.getDuration());

        publicTransport.setType(1); // 기본 대중교통 타입


        // 개인차량 정보 계산
        DirectionsRequestDto directionsRequest = new DirectionsRequestDto(
                start.getLongitude(),
                start.getLatitude(),
                end.getLongitude(),
                end.getLatitude()
        );
        DirectionsResponseDto directionsResponse = directionsService.getDrivingDirections(directionsRequest);
        RouteResponse.Routes.TransportInfo personalVehicle = new RouteResponse.Routes.TransportInfo();
        personalVehicle.setType(2); // 자동차 타입
        personalVehicle.setDuration(directionsResponse.getTotalTime() / 60); // 초를 분으로 변환

        route.setPublicTransport(publicTransport);
        route.setPersonalVehicle(personalVehicle);

        log.info("Final route object - publicTransport: {}", route.getPublicTransport().getDuration());
        log.info("Final route object - personalVehicle: {}", route.getPersonalVehicle().getDuration());


        return route;
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
