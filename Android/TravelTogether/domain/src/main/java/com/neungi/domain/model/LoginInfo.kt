package com.neungi.domain.model

data class LoginInfo(
    val userId: String,
    val userEmail: String,
    val userName: String,
    val userProfileMessage: String,
    val userProfileImg: String?
)