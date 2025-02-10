package com.travel.together.TravelTogether.tripwebsocket.dto;

import com.travel.together.TravelTogether.tripwebsocket.service.TripStateManager;

import java.util.List;

@FunctionalInterface
public interface PathGenerationCallback {
    void onPathGenerated(List<PathInfo> paths);
}