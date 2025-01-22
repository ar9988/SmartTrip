package com.neungi.domain.usecase

import com.neungi.domain.model.ApiResult
import com.neungi.domain.repository.AlbumsRepository
import javax.inject.Inject

class GetPhotoLocationsUseCase @Inject constructor(
    private val albumsRepository: AlbumsRepository
) {

    suspend fun modifyLocationName(albumId: String, photoId: String): ApiResult<Void> {
        return albumsRepository.putLocationName(albumId, photoId)
    }
}