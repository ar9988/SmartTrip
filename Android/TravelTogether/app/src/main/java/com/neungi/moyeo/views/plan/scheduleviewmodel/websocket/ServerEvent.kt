package com.neungi.moyeo.views.plan.scheduleviewmodel.websocket

data class ServerEvent(
    val tripId: Int,
    val operation: Operation,
    val timestamp: String
)

data class Operation(
    val action: Int, // move, add, delete
    val scheduleId: Int,   // UserID + timestamp
    val fromPosition: Int,     // 현재 위치
    val toPosition: Int        // 이동할 위치
)
