package com.travel.together.TravelTogether.tripwebsocket.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EditResponse {
    private String status;  // SUCCESS, ERROR
    private String operationId;
    private Integer tripId;
    private Operation operation;
    private Long timestamp;
    private Integer version;

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

    // 성공 응답 생성을 위한 팩토리 메서드
    public static EditResponse createSuccess(EditRequest request, Integer version) {
        EditResponse response = new EditResponse();
        response.setStatus("SUCCESS");
        response.setOperationId(request.getOperationId());
        response.setTripId(request.getTripId());
        response.setTimestamp(System.currentTimeMillis());
        response.setVersion(version);

        Operation operation = new Operation();
//        operation.setType(request.getOperation().getType());
        operation.setAction(request.getOperation().getAction());
        operation.setScheduleId(request.getOperation().getScheduleId());
//        operation.setDay_id(request.getOperation().getDay_id());
//        operation.setFromPosition(request.getOperation().getFromPosition());
        operation.setPositionPath(request.getOperation().getPositionPath());

        response.setOperation(operation);
        return response;
    }

    // 에러 응답 생성을 위한 팩토리 메서드
    public static EditResponse createError(EditRequest request) {
        EditResponse response = new EditResponse();
        response.setStatus("ERROR");
        response.setOperationId(request.getOperationId());
        response.setTripId(request.getTripId());
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }
}
