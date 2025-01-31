package com.neungi.data.repository.albums

import com.neungi.data.mapper.AlbumsMapper
import com.neungi.data.mapper.CommentMapper
import com.neungi.data.mapper.CommentsMapper
import com.neungi.data.mapper.PhotosMapper
import com.neungi.domain.model.ApiResult
import com.neungi.domain.model.Comment
import com.neungi.domain.model.Photo
import com.neungi.domain.model.PhotoAlbum
import com.neungi.domain.repository.AlbumsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import javax.inject.Inject

class AlbumsRepositoryImpl @Inject constructor(
    private val albumsRemoteDataSource: AlbumsRemoteDataSource
) : AlbumsRepository {

    override suspend fun getAlbums(): ApiResult<List<PhotoAlbum>> =
        try {
            val response = withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                albumsRemoteDataSource.getAlbums()
            }

            val body = response.body()
            if (response.isSuccessful && (body != null)) {
                ApiResult.success(AlbumsMapper(body))
            } else {
                ApiResult.error(response.errorBody().toString(), null)
            }

        } catch (e: Exception) {
            ApiResult.fail()
        }


    override suspend fun getAlbumPhotos(albumId: String): ApiResult<List<Photo>> =
        try {
            val response = withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                albumsRemoteDataSource.getAlbumPhotos(albumId)
            }

            val body = response.body()
            if (response.isSuccessful && (body != null)) {
                ApiResult.success(PhotosMapper(body))
            } else {
                ApiResult.error(response.errorBody().toString(), null)
            }

        } catch (e: Exception) {
            ApiResult.fail()
        }

    override suspend fun postPhoto(body: MultipartBody.Part): ApiResult<Boolean> =
        try {
            val response = withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                albumsRemoteDataSource.postPhoto(body)
            }

            val body = response.body()
            if (response.isSuccessful && (body != null)) {
                ApiResult.success(body)
            } else {
                ApiResult.error(response.errorBody().toString(), null)
            }

        } catch (e: Exception) {
            ApiResult.fail()
        }

    override suspend fun deletePhoto(albumId: String, photoId: String): ApiResult<Void> =
        try {
            val response = withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                albumsRemoteDataSource.deletePhoto(albumId, photoId)
            }

            val body = response.body()
            if (response.isSuccessful && (body != null)) {
                ApiResult.success(body)
            } else {
                ApiResult.error(response.errorBody().toString(), null)
            }

        } catch (e: Exception) {
            ApiResult.fail()
        }

    override suspend fun putLocationName(albumId: String, photoId: String): ApiResult<Void> =
        try {
            val response = withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                albumsRemoteDataSource.putLocationName(albumId, photoId)
            }

            val body = response.body()
            if (response.isSuccessful && (body != null)) {
                ApiResult.success(body)
            } else {
                ApiResult.error(response.errorBody().toString(), null)
            }

        } catch (e: Exception) {
            ApiResult.fail()
        }

    override suspend fun getPhotoComments(
        albumId: String,
        photoId: String
    ): ApiResult<List<Comment>> =
        try {
            val response = withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                albumsRemoteDataSource.getPhotoComments(albumId, photoId)
            }

            val body = response.body()
            if (response.isSuccessful && (body != null)) {
                ApiResult.success(CommentsMapper(body))
            } else {
                ApiResult.error(response.errorBody().toString(), null)
            }

        } catch (e: Exception) {
            ApiResult.fail()
        }

    override suspend fun postPhotoComments(
        albumId: String,
        photoId: String,
        body: Comment
    ): ApiResult<Comment> =
        try {
            val response = withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                albumsRemoteDataSource.postPhotoComments(albumId, photoId, body)
            }

            val body = response.body()
            if (response.isSuccessful && (body != null)) {
                ApiResult.success(CommentMapper(body))
            } else {
                ApiResult.error(response.errorBody().toString(), null)
            }

        } catch (e: Exception) {
            ApiResult.fail()
        }

    override suspend fun putPhotoComments(
        albumId: String,
        photoId: String,
        commentID: String,
        body: Comment
    ): ApiResult<Comment> =
        try {
            val response = withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                albumsRemoteDataSource.putPhotoComments(albumId, photoId, commentID, body)
            }

            val body = response.body()
            if (response.isSuccessful && (body != null)) {
                ApiResult.success(CommentMapper(body))
            } else {
                ApiResult.error(response.errorBody().toString(), null)
            }

        } catch (e: Exception) {
            ApiResult.fail()
        }

    override suspend fun deletePhotoComments(
        albumId: String,
        photoId: String,
        commentID: String,
        body: Comment
    ): ApiResult<Void> =
        try {
            val response = withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                albumsRemoteDataSource.deletePhotoComments(albumId, photoId, commentID, body)
            }

            val body = response.body()
            if (response.isSuccessful && (body != null)) {
                ApiResult.success(body)
            } else {
                ApiResult.error(response.errorBody().toString(), null)
            }

        } catch (e: Exception) {
            ApiResult.fail()
        }
}