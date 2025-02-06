package com.neungi.moyeo.util

import com.neungi.data.entity.ScheduleReceive
import com.neungi.domain.model.ScheduleData


data class Section(
    val head: ScheduleHeader,
    val items: MutableList<ScheduleData>
)

fun convertToSections(scheduleReceive: ScheduleReceive): MutableList<Section> {
    val sections = mutableListOf<Section>()

    // dayId는 0부터 시작하고, day의 순서대로 반복
    scheduleReceive.day.forEachIndexed { index, day ->
        val sectionHeader = ScheduleHeader(
            dayId = index, // dayId는 0부터 시작
            title = index.toString(),
            positionPath = (index + 1)*10000
        )

        // 각 Day의 schedule을 items로 설정
        val sectionItems = day.schedule.toMutableList()

        // Section을 생성하여 List에 추가
        val section = Section(
            head = sectionHeader,
            items = sectionItems
        )

        sections.add(section)
    }

    return sections
}