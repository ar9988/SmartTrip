package com.neungi.moyeo.util

import com.neungi.domain.model.ScheduleData

sealed class ListItem {
    data class SectionHeader(val data: ScheduleHeader) : ListItem() //Section Header
    data class Item(val data: ScheduleData, val sectionIndex: Int) : ListItem() // Section Item
}
