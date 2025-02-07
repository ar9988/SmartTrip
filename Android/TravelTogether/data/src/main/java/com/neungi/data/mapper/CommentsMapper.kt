package com.neungi.data.mapper

import com.neungi.data.entity.CommentEntity
import com.neungi.domain.model.Comment

object CommentsMapper {

    operator fun invoke(commentEntities: List<CommentEntity>): List<Comment> {
        val newComments = mutableListOf<Comment>()

        commentEntities.forEach { commentEntity ->
            newComments.add(
                Comment(
                    id = commentEntity.id.toString(),
                    albumId = commentEntity.albumId.toString(),
                    photoId = commentEntity.photoId.toString(),
                    userId = commentEntity.userId.toString(),
                    author = commentEntity.userName,
                    content = commentEntity.content,
                    createdAt = commentEntity.createdAt
                )
            )
        }

        return newComments.toList()
    }
}