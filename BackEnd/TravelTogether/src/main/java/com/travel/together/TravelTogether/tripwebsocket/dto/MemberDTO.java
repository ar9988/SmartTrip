package com.travel.together.TravelTogether.tripwebsocket.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MemberDTO {
    private String userId;
    private String name;
    private boolean owner;
    private String profileImage;
}
