package com.neungi.moyeo.views.plan

import com.neungi.moyeo.views.plan.scheduleviewmodel.ScheduleData

sealed class ListItem {
    data class SectionHeader(val title: String) : ListItem()
    data class Item(val name: ScheduleData, val sectionIndex: Int) : ListItem()
}
