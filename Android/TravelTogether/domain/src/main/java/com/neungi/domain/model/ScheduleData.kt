package com.neungi.domain.model

data class ScheduleData(
    val scheduleId: Int,
    val scheduleTitle: String,
    var positionPath: Int,
    val timeStamp: Long,
    val s3: String,
    val s4: String,
)