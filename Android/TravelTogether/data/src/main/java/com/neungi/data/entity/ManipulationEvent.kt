package com.neungi.data.entity

data class ManipulationEvent (
    val action: String,
    val tripId:Int,
    val dayOrder:Int,
    val schedule: ScheduleEntity,
    val timeStamp: Long
)