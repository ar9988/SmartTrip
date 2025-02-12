package com.travel.together.TravelTogether.invitation.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InviteResponseDto {
    private String token;
}
