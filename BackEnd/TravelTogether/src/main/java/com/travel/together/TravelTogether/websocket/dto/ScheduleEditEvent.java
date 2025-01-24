package com.travel.together.TravelTogether.websocket.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ScheduleEditEvent {

    private Integer tripId;
    private Integer dayId;
    private Integer userId;  // 편집한 사용자
    private Integer type;
    private EditTarget target;
    private Long timestamp;

    // 각 타입에 맞는 구체적인 데이터 필드
    private ScheduleData scheduleData;    // 일정 데이터
    private RouteData routeData;          // 경로 데이터
    private List<Integer> orderNumList;   // 순서 변경시 필요한 데이터

    public enum EditType {
        CREATE,     // 새로운 일정 생성
        UPDATE,     // 수정
        DELETE,     // 삭제
        REORDER     // 순서 변경
    }

    public enum EditTarget {
        SCHEDULE,   // 일정(장소) 편집
        DAY,        // 데이 편집
        ROUTE      // 경로 편집
    }


}
