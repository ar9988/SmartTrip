package com.neungi.domain.model

data class User(
    val id: String,
    val email: String,
    val passwordHash: String,
    val nickname: String,
    val profile: String,
    val createdAt: String,
    val updatedAt: String
)