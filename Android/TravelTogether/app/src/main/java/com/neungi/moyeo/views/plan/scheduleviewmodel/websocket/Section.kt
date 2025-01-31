package com.neungi.moyeo.views.plan.scheduleviewmodel.websocket

import com.neungi.moyeo.views.plan.scheduleviewmodel.ScheduleData


data class Section(
    val title: String,
    val items: MutableList<ScheduleData>
)
