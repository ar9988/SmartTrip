package com.travel.together.TravelTogether.album.dto;

import lombok.Data;

@Data
public class PhotoRequestDto {
    private int albumId;
    private Long userId;
    private Float latitude;
    private Float longitude;
    private String filePath;
    private String takenAt;
}
