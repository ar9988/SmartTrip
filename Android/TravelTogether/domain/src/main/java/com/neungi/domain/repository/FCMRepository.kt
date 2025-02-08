package com.neungi.domain.repository

import com.neungi.domain.model.ApiResult

interface FCMRepository {
    suspend fun registFCMToken(userId:String, deviceId:String, fcmToken:String)
    suspend fun updateFCMToken(userId:String, deviceId:String, fcmToken: String)
    suspend fun deleteFCMToke(userId: String, deviceId: String)
}