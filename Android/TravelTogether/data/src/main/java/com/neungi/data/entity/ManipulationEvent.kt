package com.neungi.data.entity

data class ManipulationEvent (
    val action: String,
    val tripId:Int,
    val dayId:Int,
    val timeStamp: Long,
    val schedule: ScheduleEntity
)