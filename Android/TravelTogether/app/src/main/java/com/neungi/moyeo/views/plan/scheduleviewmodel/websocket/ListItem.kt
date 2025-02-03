package com.neungi.moyeo.views.plan.scheduleviewmodel.websocket

import com.neungi.moyeo.views.plan.scheduleviewmodel.ScheduleData

sealed class ListItem {
    data class SectionHeader(val data: ScheduleHeader) : ListItem()
    data class Item(val data: ScheduleData, val sectionIndex: Int) : ListItem()
}
