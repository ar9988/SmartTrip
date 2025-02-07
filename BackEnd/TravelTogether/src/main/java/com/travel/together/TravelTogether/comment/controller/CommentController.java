package com.travel.together.TravelTogether.comment.controller;

import com.travel.together.TravelTogether.auth.entity.User;
import com.travel.together.TravelTogether.comment.dto.CommentRequestDto;
import com.travel.together.TravelTogether.comment.dto.CommentResponseDto;
import com.travel.together.TravelTogether.comment.entity.Comment;
import com.travel.together.TravelTogether.comment.service.CommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;


import java.util.List;

@Slf4j
@RestController
@RequestMapping("/albums/{albumId}/photos/{photoId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService){
        this.commentService = commentService;
    }

    // [GET]
    // 댓글 조회
    @GetMapping
    public ResponseEntity<List<CommentResponseDto>>getPhotoComment(@PathVariable("photoId") int photoId){
        List<CommentResponseDto> comments = commentService.getPhotoComments(photoId);
        return ResponseEntity.ok(comments);
    }

    // [POST]
    // 특정 사진에 댓글 달기
    @PostMapping
    public  ResponseEntity<Boolean>createComment(
            @PathVariable int albumId,
            @PathVariable int photoId,
            @RequestBody CommentRequestDto commentRequestDto,
            @AuthenticationPrincipal User user) {

        // JWT에서 사용자 정보를 추출하여 userId 획득
        String userEmail = user.getEmail();

        Boolean isComplete = commentService.createComment(albumId, photoId, userEmail, commentRequestDto.getContent());

        return ResponseEntity.ok(isComplete);
    }

    // [PUT]
    // 특정 사진에 댓글 수정
    @PutMapping("/{commentId}")
    public  ResponseEntity<Boolean>updateComment(
            @PathVariable int albumId,
            @PathVariable int photoId,
            @PathVariable int commentId,
            @RequestBody CommentRequestDto commentRequestDto,
            @AuthenticationPrincipal User user) {

        // JWT에서 사용자 정보를 추출하여 userId 획득
        String userEmail = user.getEmail();

        Boolean isComplete = commentService.updateComment(albumId, photoId, userEmail, commentId, commentRequestDto.getContent());

        return ResponseEntity.ok(isComplete);
    }
}
