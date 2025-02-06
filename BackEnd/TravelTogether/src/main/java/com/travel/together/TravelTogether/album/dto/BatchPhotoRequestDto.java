package com.travel.together.TravelTogether.album.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchPhotoRequestDto {
    private List<PhotoRequestDto> photos;
}
