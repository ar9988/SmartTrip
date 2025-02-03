package com.neungi.moyeo.views.plan.scheduleviewmodel.websocket

data class ServerReceive(
    val status:String,
    val operationId: String,
    val tripId: Int,
    val operation: Operation,
    val timestamp: Long,
    val version: Int,
)
