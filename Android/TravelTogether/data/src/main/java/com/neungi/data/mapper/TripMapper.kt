package com.neungi.data.mapper

import com.neungi.data.entity.TripEntity
import com.neungi.domain.model.Trip
import java.time.format.DateTimeFormatter


object TripMapper {

    private val formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME

    operator fun invoke(tripEntities: List<TripEntity>): List<Trip> {
        val newTrips = mutableListOf<Trip>()

        tripEntities.forEach { tripEntity ->
            newTrips.add(
                Trip(
                    id = tripEntity.id,
                    creatorId = "", // creatorId는 TripEntity에서 제공되지 않음. 필요 시 추가할 수 있음.
                    title = tripEntity.title,
                    startDate = tripEntity.startDate.toInstant().toString(), // Date -> String 변환
                    endDate = tripEntity.endDate.format(formatter), // ZonedDateTime -> String 변환
                    createdAt = tripEntity.createdAt.format(formatter), // ZonedDateTime -> String 변환
                    updatedAt = tripEntity.createdAt.format(formatter),
                    tripParticipants = tripEntity.memberCount // updatedAt은 제공되지 않지만 createdAt으로 대신 처리
                )
            )
        }

        return newTrips.toList()
    }
}