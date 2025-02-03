package com.travel.together.TravelTogether.tripwebsocket.dto;

import lombok.AllArgsConstructor;
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
    private String timestamp;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Operation {
        private String action;  // MOVE
        private Integer scheduleId;  // type이 SCHEDULE일 때
//        private Integer fromPosition;
//        private Integer toPosition;
        private Integer positionPath;
    }

}



