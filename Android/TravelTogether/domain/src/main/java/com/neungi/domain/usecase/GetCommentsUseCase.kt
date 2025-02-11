package com.neungi.domain.usecase

import com.neungi.domain.model.ApiResult
import com.neungi.domain.model.Comment
import com.neungi.domain.repository.AlbumsRepository
import kotlinx.coroutines.flow.Flow
import okhttp3.RequestBody
import javax.inject.Inject

class GetCommentsUseCase @Inject constructor(
    private val albumsRepository: AlbumsRepository
) {

    suspend fun getPhotoComments(albumId: String, photoId: String): ApiResult<List<Comment>> {
        return albumsRepository.getPhotoComments(albumId, photoId)
    }

    suspend fun submitPhotoComment(
        albumId: String,
        photoId: String,
        body: RequestBody
    ): Flow<ApiResult<Boolean>> {
        return albumsRepository.postPhotoComments(albumId, photoId, body)
    }

    suspend fun modifyPhotoComment(
        albumId: String,
        photoId: String,
        commentID: String,
        body: RequestBody
    ): ApiResult<Boolean> {
        return albumsRepository.putPhotoComments(albumId, photoId, commentID, body)
    }

    suspend fun deletePhotoComment(
        albumId: String,
        photoId: String,
        commentID: String
    ): ApiResult<Boolean> {
        return albumsRepository.deletePhotoComments(albumId, photoId, commentID)
    }
}