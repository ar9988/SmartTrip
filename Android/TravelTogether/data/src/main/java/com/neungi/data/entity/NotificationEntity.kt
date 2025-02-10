package com.neungi.data.entity

import kotlinx.serialization.Serializable

@Serializable
data class NotificationEntity(
    val id: String,
    val title: String,
    val body: String,
    val timestamp: Long
)
