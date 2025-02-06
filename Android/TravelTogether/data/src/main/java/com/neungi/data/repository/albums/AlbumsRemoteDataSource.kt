package com.neungi.data.repository.albums

import com.neungi.data.entity.AlbumEntity
import com.neungi.data.entity.CommentEntity
import com.neungi.data.entity.PhotoEntity
import com.neungi.domain.model.Comment
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

interface AlbumsRemoteDataSource {

    suspend fun getAlbums(): Response<List<AlbumEntity>>

    suspend fun getAlbumPhotos(albumId: String): Response<List<PhotoEntity>>

    suspend fun postPhoto(photos: List<MultipartBody.Part>, body: RequestBody): Response<Boolean>

    suspend fun deletePhoto(albumId: String, photoId: String): Response<Void>

    suspend fun putLocationName(albumId: String, photoId: String): Response<Void>

    suspend fun getPhotoComments(albumId: String, photoId: String): Response<List<CommentEntity>>

    suspend fun postPhotoComments(
        albumId: String,
        photoId: String,
        body: Comment
    ): Response<CommentEntity>

    suspend fun putPhotoComments(
        albumId: String,
        photoId: String,
        commentID: String,
        body: Comment
    ): Response<CommentEntity>

    suspend fun deletePhotoComments(
        albumId: String,
        photoId: String,
        commentID: String,
        body: Comment
    ): Response<Void>
}