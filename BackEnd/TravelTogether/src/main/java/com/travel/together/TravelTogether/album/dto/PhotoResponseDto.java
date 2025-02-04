package com.travel.together.TravelTogether.album.dto;

import lombok.Data;

@Data
public class PhotoResponseDto {
    private int albumId;
    private Integer userId;
    private Float latitude;
    private Float longitude;
    private String filePath;
    private String takenAt;
    private String place;
}
