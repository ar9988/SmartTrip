package com.neungi.domain.model

data class PhotoAlbum(
    val id: String,
    val tripId: String,
    val title: String,
    val imageUrl: String,
    val startDate: String,
    val endDate: String
)