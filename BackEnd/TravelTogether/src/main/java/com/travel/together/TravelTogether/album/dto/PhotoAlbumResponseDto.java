package com.travel.together.TravelTogether.album.dto;

import lombok.Data;

@Data
public class PhotoAlbumResponseDto {
    private int id;
    private int tripId;
    private String tripTitle;
    private String startDate;
    private String endDate;
    private String repImage;
}
