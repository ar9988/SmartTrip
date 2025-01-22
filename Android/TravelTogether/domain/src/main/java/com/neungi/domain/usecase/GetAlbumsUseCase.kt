package com.neungi.domain.usecase

import com.neungi.domain.model.ApiResult
import com.neungi.domain.model.PhotoAlbum
import com.neungi.domain.repository.AlbumsRepository
import javax.inject.Inject

class GetAlbumsUseCase @Inject constructor(
    private val albumsRepository: AlbumsRepository
) {

    suspend fun getAlbums(): ApiResult<List<PhotoAlbum>> {
        return albumsRepository.getAlbums()
    }
}