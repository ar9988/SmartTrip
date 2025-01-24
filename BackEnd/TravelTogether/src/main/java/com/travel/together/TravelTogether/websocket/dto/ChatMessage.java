package com.travel.together.TravelTogether.websocket.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessage {
    private String content;
    private String sender;

    // getter, setter 추가
}