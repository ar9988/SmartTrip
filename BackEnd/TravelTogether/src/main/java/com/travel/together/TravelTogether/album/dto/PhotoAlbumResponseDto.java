package com.travel.together.TravelTogether.album.dto;

import lombok.Data;

import java.util.List;

@Data
public class PhotoAlbumResponseDto {
    private int id;
    private int tripId;
    private List<PhotoResponseDto> photos;
}
