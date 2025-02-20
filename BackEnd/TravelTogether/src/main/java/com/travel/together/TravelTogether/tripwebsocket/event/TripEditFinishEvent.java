package com.travel.together.TravelTogether.tripwebsocket.event;

public class TripEditFinishEvent {
    private final Integer tripId;  // Integer 타입 사용

    public TripEditFinishEvent(Integer tripId) {
        this.tripId = tripId;
    }

    public Integer getTripId() {  // Integer 반환
        return tripId;
    }
}