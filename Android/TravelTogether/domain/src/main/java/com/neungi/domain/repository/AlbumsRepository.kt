package com.neungi.domain.repository

import com.neungi.domain.model.ApiResult
import com.neungi.domain.model.Comment
import com.neungi.domain.model.Photo
import com.neungi.domain.model.PhotoAlbum
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface AlbumsRepository {

    suspend fun getAlbums(): Flow<ApiResult<List<PhotoAlbum>>>

    suspend fun getAlbumPhotos(albumId: String): ApiResult<List<Photo>>

    suspend fun postPhoto(photos: List<MultipartBody.Part>, body: RequestBody): Flow<ApiResult<List<Photo>>>

    suspend fun deletePhoto(albumId: String, photoId: String): ApiResult<Boolean>

    suspend fun putLocationName(albumId: String, photoId: String): ApiResult<Void>

    suspend fun getPhotoComments(albumId: String, photoId: String): ApiResult<List<Comment>>

    suspend fun postPhotoComments(
        albumId: String,
        photoId: String,
        body: RequestBody
    ): Flow<ApiResult<Boolean>>

    suspend fun putPhotoComments(
        albumId: String,
        photoId: String,
        commentID: String,
        body: RequestBody
    ): ApiResult<Boolean>

    suspend fun deletePhotoComments(
        albumId: String,
        photoId: String,
        commentID: String
    ): ApiResult<Boolean>
}