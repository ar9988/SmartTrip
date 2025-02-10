package com.neungi.moyeo.util

import ScheduleReceive
import com.neungi.domain.model.ScheduleData
import com.neungi.domain.model.Trip
import java.time.format.DateTimeFormatter


data class Section(
    val head: ScheduleHeader,
    val items: MutableList<ScheduleData>
)

fun convertToSections(scheduleReceive: ScheduleReceive, trip: Trip): MutableList<Section> {
    val sections = mutableListOf<Section>()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    // dayId는 0부터 시작하고, day의 순서대로 반복
    scheduleReceive.day.forEachIndexed { index, day ->
        val sectionHeader = ScheduleHeader(
            dayId = index, // dayId는 0부터 시작
            title = (index+1).toString()+"일차 ("+trip.startDate.plusDays(((index+1).toLong())).format(formatter)+")",
            positionPath = ((index + 1)*10000-1)
        )

        // 각 Day의 schedule을 items로 설정
        val sectionItems = day.schedules.toMutableList()

        // Section을 생성하여 List에 추가
        val section = Section(
            head = sectionHeader,
            items = sectionItems
        )

        sections.add(section)
    }

    return sections
}