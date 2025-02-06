package com.travel.together.TravelTogether.album.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PhotoRequestDto {
    private int albumId;
    private Long userId;
    private Float latitude;
    private Float longitude;
    private String filePath;
    private String takenAt;
    private String place;
}
