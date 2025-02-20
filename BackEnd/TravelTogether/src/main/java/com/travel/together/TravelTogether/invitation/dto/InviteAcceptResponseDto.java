package com.travel.together.TravelTogether.invitation.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InviteAcceptResponseDto {
    private String message;
    private Integer tripId;
}
