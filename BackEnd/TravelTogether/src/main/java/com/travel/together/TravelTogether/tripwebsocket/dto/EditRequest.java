package com.travel.together.TravelTogether.tripwebsocket.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class EditRequest {
    private String operationId;
    private Integer tripId;
    private Operation operation;
    private Long timestamp;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Operation {
        private String type;  // SCHEDULE, DAY
        private String action;  // CREATE, UPDATE, DELETE, REORDER
        private Integer schedule_id;
        private OperationData data;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class OperationData {
        private String place_name;
        private Integer orderNum;
        private Float lat;
        private Float lng;
        private Integer type;
        private String start_time;
    }
}

