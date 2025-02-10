package com.neungi.data.mapper

import com.neungi.data.entity.NotificationEntity
import com.neungi.domain.model.Notification


fun NotificationEntity.toDomain() = Notification(
    id = id,
    title = title,
    body = body,
    timestamp = timestamp
)

fun Notification.toEntity() = NotificationEntity(
    id = id,
    title = title,
    body = body,
    timestamp = timestamp
)
