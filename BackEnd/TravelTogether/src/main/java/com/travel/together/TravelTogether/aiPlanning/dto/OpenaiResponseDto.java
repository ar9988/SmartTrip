package com.travel.together.TravelTogether.aiPlanning.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OpenaiResponseDto {
    private String title;
    private String startDate;
    private String endDate;
    private List<String> destination;
    private DaySchedule schedule;

    // DaySchedule 클래스 정의
    @Getter
    @Setter
    public static class DaySchedule {
        private List<DateActivities> days;
    }

    // DateActivities 클래스 정의
    @Getter
    @Setter
    public static class DateActivities {
        private String date;
        private List<Activity> activities;
    }

    // Activity 클래스 정의
    @Getter
    @Setter
    public static class Activity {
        private String name;
        private int duration;
        private int type;
        private int positionPath;
    }
}
