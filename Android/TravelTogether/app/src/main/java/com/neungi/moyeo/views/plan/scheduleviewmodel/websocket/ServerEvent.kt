package com.neungi.moyeo.views.plan.scheduleviewmodel.websocket

data class ServerEvent(
    val operationId: String,
    val tripId: Int,
    val operation: Operation,
    val timestamp: Long
)

data class Operation(
    val action: String, // move, add, delete
    val scheduleId : Int,
    val positionPath: Int,
)
