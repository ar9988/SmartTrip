package com.neungi.domain.model

data class Operation(
    val action: String, // move, add, delete
    val scheduleId : Int,
    val positionPath: Int,
)
