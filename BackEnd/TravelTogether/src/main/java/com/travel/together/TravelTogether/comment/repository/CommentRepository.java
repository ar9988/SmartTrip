package com.travel.together.TravelTogether.comment.repository;

import com.travel.together.TravelTogether.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {
    List<Comment> findByPhotoId(Integer photoId);
}