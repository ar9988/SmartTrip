package com.neungi.domain.usecase

import com.neungi.domain.model.ApiResult
import com.neungi.domain.model.Comment
import com.neungi.domain.repository.AlbumsRepository
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
        body: Comment
    ): ApiResult<Comment> {
        return albumsRepository.postPhotoComments(albumId, photoId, body)
    }

    suspend fun modifyPhotoComment(
        albumId: String,
        photoId: String,
        commentID: String,
        body: Comment
    ): ApiResult<Comment> {
        return albumsRepository.putPhotoComments(albumId, photoId, commentID, body)
    }

    suspend fun deletePhotoComment(
        albumId: String,
        photoId: String,
        commentID: String,
        body: Comment
    ): ApiResult<Void> {
        return albumsRepository.deletePhotoComments(albumId, photoId, commentID, body)
    }
}