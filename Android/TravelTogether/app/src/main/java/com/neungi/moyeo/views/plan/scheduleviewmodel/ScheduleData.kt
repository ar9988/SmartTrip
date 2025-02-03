package com.neungi.moyeo.views.plan.scheduleviewmodel

data class ScheduleData(
    val scheduleId: Int,
    val scheduleTitle: String,
    var positionPath: Int,
    val timeStamp: Long,
    val s3: String,
    val s4: String,
)