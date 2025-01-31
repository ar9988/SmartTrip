package com.neungi.data.api

import com.neungi.data.entity.AlbumEntity
import com.neungi.data.entity.CommentEntity
import com.neungi.data.entity.PhotoEntity
import com.neungi.domain.model.Comment
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AlbumsApi {

    @GET("albums")
    suspend fun getAlbums(): Response<List<AlbumEntity>>

    @GET("albums/{albumId}/photos")
    suspend fun getAlbumPhotos(
        @Path("albumId") albumId: String
    ): Response<List<PhotoEntity>>

    @Multipart
    @POST("albums/photos")
    suspend fun postPhoto(
        @Body body: MultipartBody.Part
    ): Response<Boolean>

    @DELETE("albums/{albumId}/photos/{photoId}")
    suspend fun deletePhoto(
        @Path("albumId") albumId: String,
        @Path("photoId") photoId: String
    ): Response<Void>

    @PUT("albums/{albumId}/photos/{photoId}/location")
    suspend fun putLocationName(
        @Path("albumId") albumId: String,
        @Path("photoId") photoId: String
    ): Response<Void>

    @GET("albums/{albumId}/photos/{photoId}/comments")
    suspend fun getPhotoComments(
        @Path("albumId") albumId: String,
        @Path("photoId") photoId: String
    ): Response<List<CommentEntity>>

    @POST("albums/{albumId}/photos/{photoId}/comments")
    suspend fun postPhotoComments(
        @Path("albumId") albumId: String,
        @Path("photoId") photoId: String,
        @Body body: Comment
    ): Response<CommentEntity>

    @PUT("albums/{albumId}/photos/{photoId}/comments/{commentId}")
    suspend fun putPhotoComments(
        @Path("albumId") albumId: String,
        @Path("photoId") photoId: String,
        @Path("commentId") commentID: String,
        @Body body: Comment
    ): Response<CommentEntity>

    @DELETE("albums/{albumId}/photos/{photoId}/comments/{commentId}")
    suspend fun deletePhotoComments(
        @Path("albumId") albumId: String,
        @Path("photoId") photoId: String,
        @Path("commentId") commentID: String,
        @Body body: Comment
    ): Response<Void>
}