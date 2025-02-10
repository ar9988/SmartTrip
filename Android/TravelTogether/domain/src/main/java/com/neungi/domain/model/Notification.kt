package com.neungi.domain.model

import java.util.UUID

data class Notification(
    val id: String,
    val title: String,
    val body: String,
    val timestamp: Long
) {
    companion object {
        fun create(title: String, body: String, timestamp: Long = System.currentTimeMillis()) =
            Notification(
                id = UUID.randomUUID().toString(),
                title = title,
                body = body,
                timestamp = timestamp
            )
    }
}