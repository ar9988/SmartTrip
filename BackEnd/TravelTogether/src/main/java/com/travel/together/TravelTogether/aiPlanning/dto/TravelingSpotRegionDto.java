package com.travel.together.TravelTogether.aiPlanning.dto;

import com.fasterxml.jackson.databind.ser.std.StdKeySerializers;
import lombok.Data;

@Data
public class TravelingSpotRegionDto {
    private String title;
    private String imageUrl;
    private String address;
    private String overView;
    private String contentId;
    private Boolean isFollowed;
}
