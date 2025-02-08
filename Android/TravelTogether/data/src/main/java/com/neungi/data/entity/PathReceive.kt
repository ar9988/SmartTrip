package com.neungi.data.entity

import com.neungi.domain.model.Path

data class PathReceive(
    val tripId: Int,
    val scheduleId: Int,
    val newPosition: Int,
    val paths: List<Path>
)