package com.neungi.data.entity

data class AddEvent (
    val action: String = "ADD",
    val tripId:Int,
    val dayId:Int,
    val schedule: ScheduleEntity
)