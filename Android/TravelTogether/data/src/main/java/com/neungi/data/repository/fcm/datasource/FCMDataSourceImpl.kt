package com.neungi.data.repository.fcm.datasource

import com.neungi.data.api.FCMApi
import retrofit2.Response
import javax.inject.Inject

class FCMDataSourceImpl @Inject constructor(
    private val fcmApi: FCMApi
) : FCMDataSource{
    override suspend fun registFCMToken(
        userId: String,
        deviceId: String,
        fcmToken: String
    ): Response<Void> = fcmApi.registFCMToken(userId, deviceId, fcmToken)

    override suspend fun updateFCMToken(
        userId: String,
        deviceId: String,
        fcmToken: String
    ): Response<Void> = fcmApi.updateFCMToken(userId, deviceId, fcmToken)

    override suspend fun deleteFCMToken(userId: String, deviceId: String): Response<Void> = fcmApi.deleteFCMToken(userId, deviceId)


}