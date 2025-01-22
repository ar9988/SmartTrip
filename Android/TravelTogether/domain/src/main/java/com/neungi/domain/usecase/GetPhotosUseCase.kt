package com.neungi.domain.usecase

import com.neungi.domain.model.ApiResult
import com.neungi.domain.model.Photo
import com.neungi.domain.repository.AlbumsRepository
import javax.inject.Inject

class GetPhotosUseCase @Inject constructor(
    private val albumsRepository: AlbumsRepository
) {

    suspend fun getPhotos(albumId: String): ApiResult<List<Photo>> {
        return albumsRepository.getAlbumPhotos(albumId)
    }

    suspend fun submitPhoto(photo: Photo): ApiResult<Boolean> {
        return albumsRepository.postPhoto(photo)
    }

    suspend fun deletePhoto(albumId: String, photoId: String): ApiResult<Void> {
        return albumsRepository.deletePhoto(albumId, photoId)
    }
}