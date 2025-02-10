package com.travel.together.TravelTogether.comment.service;

import com.travel.together.TravelTogether.album.entity.Photo;
import com.travel.together.TravelTogether.album.repository.PhotoRepository;
import com.travel.together.TravelTogether.auth.entity.User;
import com.travel.together.TravelTogether.auth.repository.UserRepository;
import com.travel.together.TravelTogether.comment.dto.CommentRequestDto;
import com.travel.together.TravelTogether.comment.dto.CommentResponseDto;
import com.travel.together.TravelTogether.comment.entity.Comment;
import com.travel.together.TravelTogether.comment.repository.CommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentRepository  commentRepository;
    private final UserRepository userRepository;
    private final PhotoRepository photoRepository;

    public CommentService(CommentRepository commentRepository, UserRepository userRepository,
                          PhotoRepository photoRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.photoRepository = photoRepository;
    }

    // 댓글 조회
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getPhotoComments(Integer photoId) {
        List<Comment> comments = commentRepository.findByPhotoId(photoId);

        return comments.stream().map(comment -> {
            CommentResponseDto responseDto = new CommentResponseDto();
            responseDto.setCommentId(comment.getId());
            responseDto.setUserId(comment.getUser().getUserId());
            responseDto.setUserName(comment.getUser().getName());
            responseDto.setPhotoId(comment.getPhoto().getId());
            responseDto.setAlbumId(comment.getPhoto().getAlbum().getId());
            responseDto.setContent(comment.getContent());
            responseDto.setProfileImage(comment.getUser().getProfile_image());
            responseDto.setCreatedAt(comment.getCreatedAt());
            responseDto.setUpdatedAt(comment.getUpdatedAt());
            return responseDto;
        }).collect(Collectors.toList());
    }

    // 댓글 생성
    @Transactional
    public Boolean createComment(int albumId, int photoId, String userEmail, String content) {
        // Photo 엔티티 조회
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found with id: " + photoId));

        // (선택사항) Photo에 앨범 정보가 있고 검증이 필요하다면 아래와 같이 albumId 일치 여부를 체크합니다.
        if (photo.getAlbum() == null || photo.getAlbum().getId() != albumId) {
            throw new RuntimeException("Photo with id " + photoId + " does not belong to album " + albumId);
        }

        // User 엔티티 조회 (JWT에서 전달받은 userId 사용)
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userEmail));

        // Comment 엔티티 생성
        Comment comment = new Comment();
        comment.setPhoto(photo);
        comment.setUser(user);
        comment.setContent(content);

        // DB에 저장 후 반환
        commentRepository.save(comment);
        return true;
    }

    // 댓글 수정
    @Transactional
    public Boolean updateComment(int albumId, int photoId,  String userEmail, int commentId, String content) {
        // Photo 엔티티 조회
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found with id: " + photoId));

        // Photo에 앨범 정보가 있고 검증이 필요하다면 아래와 같이 albumId 일치 여부를 체크합니다.
        if (photo.getAlbum() == null || photo.getAlbum().getId() != albumId) {
            throw new RuntimeException("Photo with id " + photoId + " does not belong to album " + albumId);
        }

        // User 엔티티 조회 (JWT에서 전달받은 userId 사용)
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userEmail));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You are not the owner of this comment");
        }
        // Comment 엔티티 수정
        comment.setContent(content);

        // DB에 저장 후 반환
        commentRepository.save(comment);
        return true;
    }

    @Transactional
    public Boolean deleteComment(int albumId, int photoId,  String userEmail, int commentId) {
        // Photo 엔티티 조회
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found with id: " + photoId));

        // Photo에 앨범 정보가 있고 검증이 필요하다면 아래와 같이 albumId 일치 여부를 체크합니다.
        if (photo.getAlbum() == null || photo.getAlbum().getId() != albumId) {
            throw new RuntimeException("Photo with id " + photoId + " does not belong to album " + albumId);
        }

        // User 엔티티 조회 (JWT에서 전달받은 userId 사용)
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userEmail));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You are not the owner of this comment");
        }


        // DB에 저장 후 반환
        commentRepository.delete(comment);
        return true;
    }
}
