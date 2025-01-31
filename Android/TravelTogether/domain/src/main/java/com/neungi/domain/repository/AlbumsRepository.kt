package com.neungi.domain.repository

import com.neungi.domain.model.ApiResult
import com.neungi.domain.model.Comment
import com.neungi.domain.model.Photo
import com.neungi.domain.model.PhotoAlbum
import okhttp3.MultipartBody

interface AlbumsRepository {

    suspend fun getAlbums(): ApiResult<List<PhotoAlbum>>

    suspend fun getAlbumPhotos(albumId: String): ApiResult<List<Photo>>

    suspend fun postPhoto(body: MultipartBody.Part): ApiResult<Boolean>

    suspend fun deletePhoto(albumId: String, photoId: String): ApiResult<Void>

    suspend fun putLocationName(albumId: String, photoId: String): ApiResult<Void>

    suspend fun getPhotoComments(albumId: String, photoId: String): ApiResult<List<Comment>>

    suspend fun postPhotoComments(
        albumId: String,
        photoId: String,
        body: Comment
    ): ApiResult<Comment>

    suspend fun putPhotoComments(
        albumId: String,
        photoId: String,
        commentID: String,
        body: Comment
    ): ApiResult<Comment>

    suspend fun deletePhotoComments(
        albumId: String,
        photoId: String,
        commentID: String,
        body: Comment
    ): ApiResult<Void>
}