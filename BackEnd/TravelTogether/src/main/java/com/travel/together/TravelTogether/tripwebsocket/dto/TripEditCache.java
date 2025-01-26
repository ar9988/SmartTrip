package com.travel.together.TravelTogether.tripwebsocket.dto;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class TripEditCache {
    // tripId를 키로 하고, 해당 여행의 편집 히스토리를 값으로 가지는 Map
    // editHistory는 COncurrentHashMap으로 메모리상에서만존재함
    private final Map<String, List<EditRequest>> editHistory = new ConcurrentHashMap<>();

    // 편집 내용들은 CopyOnWriteArrayList에 임시저장
    public void addEdit(String tripId, EditRequest edit) {
        editHistory.computeIfAbsent(tripId, k -> new CopyOnWriteArrayList<>())
                .add(edit);
    }

    public List<EditRequest> getAndRemoveEdits(String tripId) {
        return editHistory.remove(tripId);
    }


}
