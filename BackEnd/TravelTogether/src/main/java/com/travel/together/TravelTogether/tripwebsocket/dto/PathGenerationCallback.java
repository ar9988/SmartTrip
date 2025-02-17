package com.travel.together.TravelTogether.tripwebsocket.dto;

import java.util.List;

@FunctionalInterface
public interface PathGenerationCallback {
    void onPathGenerated(List<PathInfo> paths);
}