package com.travel.together.TravelTogether.tripwebsocket.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// EditResponse.java
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
        private String type;  // SCHEDULE, DAY
        private String action;  // UPDATE, DELETE, REORDER
        private Integer targetId;
        private OperationResult result;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class OperationResult {
        private Integer schedule_id;
        private Integer from;
        private Integer to;
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
        operation.setType(request.getOperation().getType());
        operation.setAction(request.getOperation().getAction());
        operation.setTargetId(request.getOperation().getSchedule_id());

        response.setOperation(operation);
        return response;
    }

    // 에러 응답 생성을 위한 팩토리 메서드
    public static EditResponse createError(EditRequest request, String errorMessage) {
        EditResponse response = new EditResponse();
        response.setStatus("ERROR");
        response.setOperationId(request.getOperationId());
        response.setTripId(request.getTripId());
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }
}