package com.neungi.domain.model

data class ServerReceive(
    val status:String,
    val operationId: String,
    val tripId: Int,
    val operation: Operation,
    val timestamp: Long,
    val version: Int,
)