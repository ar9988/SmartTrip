package com.travel.together.TravelTogether.trip.dto;

import com.travel.together.TravelTogether.trip.entity.TripMember;
import lombok.Getter;

@Getter
public class MemberDto {
    private String userId;
    private String name;
    private boolean isOwner;

    public MemberDto(TripMember member) {
        this.userId = member.getUser().getId().toString();
        this.name = member.getUser().getName();
        this.isOwner = member.getIsOwner();
    }
}