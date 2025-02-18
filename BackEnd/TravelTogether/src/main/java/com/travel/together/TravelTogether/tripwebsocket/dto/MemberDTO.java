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
// 초기 유저 말고 새로 초대된 유저 감지될때마다 전체 유저목록 보내주기
