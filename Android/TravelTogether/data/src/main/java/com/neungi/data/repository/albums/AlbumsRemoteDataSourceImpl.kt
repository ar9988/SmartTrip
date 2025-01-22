package com.neungi.data.repository.albums

import com.neungi.data.api.AlbumsApi
import com.neungi.data.entity.AlbumEntity
import com.neungi.data.entity.CommentEntity
import com.neungi.data.entity.PhotoEntity
import com.neungi.domain.model.Comment
import com.neungi.domain.model.Photo
import retrofit2.Response
import javax.inject.Inject

class AlbumsRemoteDataSourceImpl @Inject constructor(
    private val albumsApi: AlbumsApi
) : AlbumsRemoteDataSource {

    override suspend fun getAlbums(): Response<List<AlbumEntity>> =
        albumsApi.getAlbums()

    override suspend fun getAlbumPhotos(albumId: String): Response<List<PhotoEntity>> =
        albumsApi.getAlbumPhotos(albumId)

    override suspend fun postPhoto(body: Photo): Response<Boolean> =
        albumsApi.postPhoto(body)

    override suspend fun deletePhoto(albumId: String, photoId: String): Response<Void> =
        albumsApi.deletePhoto(albumId, photoId)

    override suspend fun putLocationName(albumId: String, photoId: String): Response<Void> =
        albumsApi.putLocationName(albumId, photoId)

    override suspend fun getPhotoComments(
        albumId: String,
        photoId: String
    ): Response<List<CommentEntity>> =
        albumsApi.getPhotoComments(albumId, photoId)

    override suspend fun postPhotoComments(
        albumId: String,
        photoId: String,
        body: Comment
    ): Response<CommentEntity> =
        albumsApi.postPhotoComments(albumId, photoId, body)

    override suspend fun putPhotoComments(
        albumId: String,
        photoId: String,
        commentID: String,
        body: Comment
    ): Response<CommentEntity> =
        albumsApi.putPhotoComments(albumId, photoId, commentID, body)

    override suspend fun deletePhotoComments(
        albumId: String,
        photoId: String,
        commentID: String,
        body: Comment
    ): Response<Void> =
        albumsApi.deletePhotoComments(albumId, photoId, commentID, body)
}