package com.neungi.data.mapper

import com.neungi.data.entity.RecommendPlaceResponse
import com.neungi.domain.model.Place

object RecommendPlaceMapper {

    operator fun invoke(recommendPlaceResponse: List<RecommendPlaceResponse>): List<Place> {
        val newRecommendPlaces = mutableListOf<Place>()
        recommendPlaceResponse.forEach { recommendPlace ->
            newRecommendPlaces.add(
                Place(
                    contentId = recommendPlace.contentId,
                    placeName = recommendPlace.title,
                    address = recommendPlace.address,
                    imageUrl = recommendPlace.imageUrl,
                    overView = recommendPlace.overView?:"",
                    lat = null,
                    lng = null,
                    isFollowed = recommendPlace.isFollowed
                )
            )
        }

        return newRecommendPlaces.toList()
    }
}