package com.neungi.data.mapper

import com.neungi.data.entity.PhotoEntity
import com.neungi.domain.model.Photo

object PhotosMapper {

    operator fun invoke(photoEntities: List<PhotoEntity>): List<Photo> {
        val newPhotos = mutableListOf<Photo>()

        photoEntities.forEach { photoEntity ->
            newPhotos.add(
                Photo(
                    id = photoEntity.id.toString(),
                    albumId = "",
                    photoPlace = "",
                    userId = "",
                    filePath = photoEntity.url,
                    latitude = photoEntity.latitude,
                    longitude = photoEntity.longitude,
                    takenAt = photoEntity.createdAt,
                    uploadedAt = ""
                )
            )
        }

        return newPhotos.toList()
    }
}