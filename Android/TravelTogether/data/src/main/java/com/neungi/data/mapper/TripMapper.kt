package com.neungi.data.mapper

import TripEntity
import com.neungi.domain.model.Trip
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object TripMapper {
    operator fun invoke(tripEntities: List<TripEntity>): List<Trip> {
        val newTrips = mutableListOf<Trip>()

        tripEntities.forEach { tripEntity ->
            newTrips.add(
                Trip(
                    id = tripEntity.id,
                    creatorId = "", // creatorId는 TripEntity에서 제공되지 않음. 필요 시 추가할 수 있음.
                    title = tripEntity.title,
                    startDate =  tripEntity.startDate  , // Date -> LocalDateTime 변환
                    endDate =  tripEntity.endDate  , // String -> LocalDateTime 변환
                    createdAt =  tripEntity.createdAt  , // String -> LocalDateTime 변환
                    updatedAt =  tripEntity.createdAt  , // updatedAt도 createdAt으로 처리
                    tripParticipants = tripEntity.memberCount // updatedAt은 제공되지 않지만 createdAt으로 대신 처리
                )
            )
        }

        return newTrips.toList()
    }
}
