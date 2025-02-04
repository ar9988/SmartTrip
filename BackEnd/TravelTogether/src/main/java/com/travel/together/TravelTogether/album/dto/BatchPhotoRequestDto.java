package com.travel.together.TravelTogether.album.dto;

import lombok.Data;
import java.util.List;

@Data
public class BatchPhotoRequestDto {
    private List<PhotoRequestDto> photos;
}
