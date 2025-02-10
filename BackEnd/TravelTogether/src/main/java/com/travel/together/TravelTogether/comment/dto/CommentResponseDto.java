package com.travel.together.TravelTogether.comment.dto;

import lombok.Data;

import java.security.Principal;
import java.time.LocalDateTime;

@Data
public class CommentResponseDto {
    private int commentId;
    private int userId;
    private String userName;
    private int albumId;
    private int photoId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
