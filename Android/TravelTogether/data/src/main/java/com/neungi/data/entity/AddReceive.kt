package com.neungi.data.entity

import com.neungi.domain.model.ScheduleData
import kotlin.reflect.jvm.internal.impl.incremental.components.Position

data class AddReceive(
    val newPosition: Int,
    val scheduleData: ScheduleData
)
