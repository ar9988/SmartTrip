package com.neungi.data.mapper

import com.neungi.data.entity.CommentEntity
import com.neungi.domain.model.Comment

object CommentMapper {

    operator fun invoke(commentEntity: CommentEntity): Comment {
        return Comment(
            id = commentEntity.id.toString(),
            photoId = "",
            author = commentEntity.userId,
            content = commentEntity.content,
            createdAt = commentEntity.createdAt,
            updatedAt = ""
        )
    }
}