package com.neungi.data.repository.fcm

import com.neungi.data.mapper.PlaceMapper
import com.neungi.data.repository.fcm.datasource.FCMDataSource
import com.neungi.domain.model.ApiResult
import com.neungi.domain.repository.FCMRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FCMRepositoryImpl @Inject constructor(
    private val fcmDataSource: FCMDataSource
): FCMRepository{
    override suspend fun registFCMToken(userId: String, deviceId: String, fcmToken: String) {
        try {
            val response = withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                fcmDataSource.registFCMToken(userId,deviceId, fcmToken)
            }

        } catch (e: Exception) {
        }
    }

    override suspend fun updateFCMToken(userId: String, deviceId: String, fcmToken: String) {
        try {
            val response = withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                fcmDataSource.updateFCMToken(userId,deviceId, fcmToken)
            }

        } catch (e: Exception) {
        }
    }

    override suspend fun deleteFCMToke(userId: String, deviceId: String) {
        try {
            val response = withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                fcmDataSource.deleteFCMToken(userId,deviceId)
            }

        } catch (e: Exception) {
        }
    }

}