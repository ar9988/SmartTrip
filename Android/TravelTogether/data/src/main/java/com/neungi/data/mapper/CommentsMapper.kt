package com.neungi.data.mapper

import com.neungi.data.entity.CommentEntity
import com.neungi.data.util.CommonUtils.formatDateTimeHourMinuteSecond
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
                    profileImage = commentEntity.profileImage ?: "",
                    content = commentEntity.content,
                    createdAt = formatDateTimeHourMinuteSecond(commentEntity.createdAt)
                )
            )
        }

        return newComments.toList()
    }
}