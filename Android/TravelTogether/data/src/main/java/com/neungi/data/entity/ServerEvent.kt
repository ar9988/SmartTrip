package com.neungi.data.entity

import com.neungi.domain.model.Operation

data class ServerEvent(
    val operationId: String,
    val tripId: Int,
    val operation: Operation,
    val timestamp: Long
)