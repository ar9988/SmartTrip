package com.neungi.moyeo.views.plan.scheduleviewmodel.websocket

data class ServerReceive(
    val status:String,
    val operationId: Int,
    val tripId: Int,
    val operation: Operation,
    val timestamp: String,
    val version: Int,
)
