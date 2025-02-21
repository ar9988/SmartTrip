package com.neungi.data.mapper


import com.neungi.data.entity.PlaceEntity
import com.neungi.domain.model.Festival
import com.neungi.domain.model.Place

object PlaceMapper {
    operator fun invoke(placeEntities: List<PlaceEntity>): List<Place> {
        val newPlaces = mutableListOf<Place>()

        placeEntities.forEach { place ->
            newPlaces.add(
                Place(
                    placeName = place.placeName,
                    address = place.address,
                    lat = place.latitude,
                    lng = place.longitude
                )
            )
        }

        return newPlaces.toList()
    }
}