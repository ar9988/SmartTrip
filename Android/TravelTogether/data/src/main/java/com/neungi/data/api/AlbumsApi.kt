package com.neungi.data.api

import com.neungi.data.entity.AlbumEntity
import com.neungi.data.entity.CommentEntity
import com.neungi.data.entity.PhotoEntity
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AlbumsApi {

    @GET("albums")
    fun getAlbums(): Response<List<AlbumEntity>>

    @GET("albums/{albumId}/photos")
    fun getAlbumPhotos(
        @Path("albumId") albumId: String
    ): Response<List<PhotoEntity>>

    @POST("albums/photos")
    fun postPhoto(
        @Body body: PhotoEntity
    ): Response<Boolean>

    @DELETE("albums/{albumId}/photos/{photoId}")
    fun deletePhoto(
        @Path("albumId") albumId: String,
        @Path("photoId") photoId: String
    ): Response<Void>

    @PUT("albums/{albumId}/photos/{photoId}/location")
    fun putLocationName(
        @Path("albumId") albumId: String,
        @Path("photoId") photoId: String
    ): Response<Void>

    @GET("albums/{albumId}/photos/{photoId}/comments")
    fun getPhotoComments(
        @Path("albumId") albumId: String,
        @Path("photoId") photoId: String
    ): Response<List<CommentEntity>>

    @POST("albums/{albumId}/photos/{photoId}/comments")
    fun postPhotoComments(
        @Path("albumId") albumId: String,
        @Path("photoId") photoId: String,
        @Body body: CommentEntity
    ): Response<CommentEntity>

    @PUT("albums/{albumId}/photos/{photoId}/comments/{commentId}")
    fun putPhotoComments(
        @Path("albumId") albumId: String,
        @Path("photoId") photoId: String,
        @Path("commentId") commentID: String,
        @Body body: CommentEntity
    ): Response<CommentEntity>

    @DELETE("albums/{albumId}/photos/{photoId}/comments/{commentId}")
    fun deletePhotoComments(
        @Path("albumId") albumId: String,
        @Path("photoId") photoId: String,
        @Path("commentId") commentID: String,
        @Body body: CommentEntity
    ): Response<Void>
}