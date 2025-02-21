package com.neungi.data.repository.fcm.datasource

import com.neungi.data.entity.SearchPlaceResponse
import com.squareup.moshi.JsonReader.Token
import retrofit2.Response

interface FCMDataSource {
    suspend fun registFCMToken(userId:String, deviceId:String,fcmToken: String): Response<Void>

    suspend fun updateFCMToken(userId:String, deviceId:String,fcmToken: String): Response<Void>

    suspend fun deleteFCMToken(userId:String, deviceId:String): Response<Void>
}