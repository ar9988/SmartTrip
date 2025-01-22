package com.neungi.domain.model

data class Trip(
    val id: String,
    val creatorId: String,
    val title: String,
    val startDate: String,
    val endDate: String,
    val createdAt: String,
    val updatedAt: String
)