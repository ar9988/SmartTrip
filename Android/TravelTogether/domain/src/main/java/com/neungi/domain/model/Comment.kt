package com.neungi.domain.model

data class Comment(
    val id: String,
    val albumId: String,
    val photoId: String,
    val userId: String,
    val author: String,
    val content: String,
    val createdAt: String
)