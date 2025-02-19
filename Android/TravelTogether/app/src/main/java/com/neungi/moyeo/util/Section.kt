package com.neungi.moyeo.util

import com.neungi.data.entity.Member
import com.neungi.data.entity.ScheduleReceive
import com.neungi.domain.model.ScheduleData
import com.neungi.domain.model.Trip
import com.neungi.moyeo.util.CommonUtils.formatZonedDateTimeWithZone
import timber.log.Timber
import java.time.format.DateTimeFormatter

data class Section(
    val head: ScheduleHeader,
    val items: MutableList<ScheduleData>
)

fun convertToSections(scheduleReceive: ScheduleReceive, trip: Trip): MutableList<Section> {

    val sections = mutableListOf<Section>()
    scheduleReceive.day.forEachIndexed { index, day ->
        val sectionHeader = ScheduleHeader(
            dayId = index + 1,
            title = (index + 1).toString() + "일차 (" + formatZonedDateTimeWithZone(
                trip.startDate.plusDays(
                    ((index).toLong())
                )
            ) + ")",
            positionPath = ((index + 1) * 10000 - 1),
            startTime = day.startTime
            // 경계값과 동일한 일정이 생겨도 헤더가 위로 오게 하기위함 Item의 path값을 계산할땐 +1해서 계산
        // 경계값과 동일한 일정이 생겨도 헤더가 위로 오게 하기위함 Item의 path값을 계산할땐 +1해서 계산
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
