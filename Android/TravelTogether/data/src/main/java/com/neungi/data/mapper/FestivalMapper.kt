package com.neungi.data.mapper

import com.neungi.data.entity.CommentEntity
import com.neungi.data.entity.FestivalEntity
import com.neungi.domain.model.Comment
import com.neungi.domain.model.Festival

object FestivalMapper {
    operator fun invoke(festivalEntity: List<FestivalEntity>): List<Festival> {
        val newFestivals = mutableListOf<Festival>()

        festivalEntity.forEach { festivalEntity ->
            newFestivals.add(
                Festival(
                    title = festivalEntity.title,
                    imageUrl = festivalEntity.imageUrl,
                    address =  festivalEntity.address,
                    startDate = festivalEntity.eventStartDate,
                    endDate = festivalEntity.eventEndDate,
                    contentId = festivalEntity.contentId
                )
            )
        }

        return newFestivals.toList()
    }
}