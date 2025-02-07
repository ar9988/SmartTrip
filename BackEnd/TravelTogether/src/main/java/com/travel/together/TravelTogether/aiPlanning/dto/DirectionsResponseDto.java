package com.travel.together.TravelTogether.aiPlanning.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DirectionsResponseDto {
    private Integer totalTime; // 총 소요 시간
    private DirectionPath directionPath; // 경로 정보

    @Getter
    @Setter
    @NoArgsConstructor
    public static class DirectionPath {
        private List<PathPoint> path; // 경로 리스트
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class PathPoint {
        private Double longitude; // 경도
        private Double latitude;  // 위도

        public PathPoint(Double latitude, Double longitude) {
            this.longitude = longitude;
            this.latitude = latitude;
        }
    }
}
