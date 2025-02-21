package com.neungi.data.mapper

import com.neungi.data.entity.PhotoEntity
import com.neungi.data.util.CommonUtils.formatDateTimeHourMinute
import com.neungi.domain.model.Photo

object PhotosMapper {

    operator fun invoke(photoEntities: List<PhotoEntity>): List<Photo> {
        val newPhotos = mutableListOf<Photo>()

        photoEntities.forEach { photoEntity ->
            newPhotos.add(
                Photo(
                    id = photoEntity.id.toString(),
                    albumId = photoEntity.albumId.toString(),
                    photoPlace = photoEntity.place,
                    userId = photoEntity.userId.toString(),
                    filePath = photoEntity.url,
                    latitude = photoEntity.latitude,
                    longitude = photoEntity.longitude,
                    takenAt = formatDateTimeHourMinute(photoEntity.takenAt ?: ""),
                    uploadedAt = ""
                )
            )
        }

        return newPhotos.toList()
    }
}