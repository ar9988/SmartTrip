package com.neungi.data.mapper

import com.neungi.data.entity.CommentEntity
import com.neungi.data.entity.FestivalEntity
import com.neungi.domain.model.Comment
import com.neungi.domain.model.Festival
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object FestivalMapper {
    operator fun invoke(festivalEntities: List<FestivalEntity>): List<Festival> {
        val newFestivals = mutableListOf<Festival>()

        festivalEntities.forEach { festival ->
            newFestivals.add(
                Festival(
                    title = festival.title,
                    imageUrl = festival.imageUrl,
                    address =  festival.address,
                    startDate = formatDate(festival.eventStartDate),
                    endDate = formatDate(festival.eventEndDate),
                    contentId = festival.contentId
                )
            )
        }

        return newFestivals.toList()
    }

    private val inputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    private val outputFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

    private fun formatDate(date: String): String {
        return try {
            val localDate = LocalDate.parse(date, inputFormatter)
            localDate.format(outputFormatter)
        } catch (e: Exception) {
            date  // 파싱 실패 시 원본 반환
        }
    }

}