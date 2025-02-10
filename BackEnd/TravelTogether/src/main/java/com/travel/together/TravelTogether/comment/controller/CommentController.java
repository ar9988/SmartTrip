package com.travel.together.TravelTogether.comment.controller;

import com.travel.together.TravelTogether.auth.entity.User;
import com.travel.together.TravelTogether.auth.jwt.JwtTokenProvider;
import com.travel.together.TravelTogether.auth.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public CommentController(CommentService commentService,
                             UserRepository userRepository,
                             JwtTokenProvider jwtTokenProvider){
        this.commentService = commentService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
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
            @RequestHeader("Authorization") String token) {

        String jwtToken = token.replace("Bearer", "").trim();

        String userEmail = jwtTokenProvider.getEmailFromToken(jwtToken);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

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
            @RequestHeader("Authorization") String token) {

        String jwtToken = token.replace("Bearer", "").trim();

        String userEmail = jwtTokenProvider.getEmailFromToken(jwtToken);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Boolean isComplete = commentService.updateComment(albumId, photoId, userEmail, commentId, commentRequestDto.getContent());

        return ResponseEntity.ok(isComplete);
    }

    // [DELETE]
    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Boolean> deleteComment(
            @PathVariable int albumId,
            @PathVariable Integer photoId,
            @PathVariable int commentId,
            @RequestHeader("Authorization") String token)
     {
         String jwtToken = token.replace("Bearer", "").trim();

         String userEmail = jwtTokenProvider.getEmailFromToken(jwtToken);
         User user = userRepository.findByEmail(userEmail)
                 .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

         Boolean isDeleted = commentService.deleteComment(albumId, photoId, userEmail, commentId);
         return ResponseEntity.ok(isDeleted);
    }
}
