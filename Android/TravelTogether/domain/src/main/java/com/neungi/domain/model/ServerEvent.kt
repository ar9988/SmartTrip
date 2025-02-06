package com.neungi.domain.model

data class ServerEvent(
    val operationId: String,
    val tripId: Int,
    val operation: Operation,
    val timestamp: Long
)