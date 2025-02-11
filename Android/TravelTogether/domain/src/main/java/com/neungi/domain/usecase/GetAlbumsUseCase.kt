package com.neungi.domain.usecase

import com.neungi.domain.model.ApiResult
import com.neungi.domain.model.PhotoAlbum
import com.neungi.domain.repository.AlbumsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAlbumsUseCase @Inject constructor(
    private val albumsRepository: AlbumsRepository
) {

    suspend fun getAlbums(): Flow<ApiResult<List<PhotoAlbum>>> {
        return albumsRepository.getAlbums()
    }
}