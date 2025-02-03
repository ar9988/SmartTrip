package com.neungi.moyeo.util

import com.neungi.domain.model.ScheduleData


data class Section(
    val head: ScheduleHeader,
    val items: MutableList<ScheduleData>
)
