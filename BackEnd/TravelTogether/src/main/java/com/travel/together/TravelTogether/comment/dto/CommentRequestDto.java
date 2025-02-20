package com.travel.together.TravelTogether.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentRequestDto {
    private int albumId;
    private int photoId;
    private Long userId;
    private String content;
}
