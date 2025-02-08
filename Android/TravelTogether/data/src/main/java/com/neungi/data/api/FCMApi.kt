package com.neungi.data.api

import com.neungi.data.entity.CommentEntity
import com.neungi.domain.model.Comment
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface FCMApi {

    @POST("/fcm/token")
    suspend fun registFCMToken(
        @Query("userId") userId: String,
        @Query("deviceId") deviceId: String,
        @Query("fcmToken") fcmToken: String,
    ): Response<Void>

    @PUT("/fcm/token")
    suspend fun updateFCMToken(
        @Query("userId") userId: String,
        @Query("deviceId") deviceId: String,
        @Query("fcmToken") fcmToken: String,
    ): Response<Void>

    @DELETE("/fcm/token")
    suspend fun deleteFCMToken(
        @Query("userId") userId: String,
        @Query("deviceId") deviceId: String,
    ): Response<Void>
}