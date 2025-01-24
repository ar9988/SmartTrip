package com.neungi.domain.model

data class Photo(
    val id: String,
    val albumId: String,
    val photoPlace: String,
    val userId: String,
    val filePath: String,
    val latitude: Double,
    val longitude: Double,
    val takenAt: String,
    val uploadedAt: String
)