package com.neungi.domain.model

data class Comment(
    val id: String,
    val photoId: String,
    val author: String,
    val content: String,
    val createdAt: String,
    val updatedAt: String
)