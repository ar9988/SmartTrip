package com.neungi.data.mapper

import com.neungi.data.entity.CommentEntity
import com.neungi.domain.model.Comment

object CommentMapper {

    operator fun invoke(commentEntity: CommentEntity): Comment {
        return Comment(
            id = commentEntity.id.toString(),
            albumId = commentEntity.albumId.toString(),
            photoId = commentEntity.photoId.toString(),
            userId = commentEntity.userId.toString(),
            author = commentEntity.userName,
            content = commentEntity.content,
            createdAt = commentEntity.createdAt
        )
    }
}