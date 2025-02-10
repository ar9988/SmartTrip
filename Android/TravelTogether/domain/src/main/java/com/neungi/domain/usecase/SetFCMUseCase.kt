package com.neungi.domain.usecase

import com.neungi.domain.repository.FCMRepository
import javax.inject.Inject

class SetFCMUseCase @Inject constructor(
    private val fcmRepository: FCMRepository
) {
    suspend fun registFCMToken(userId:String, deviceId:String, fcmToken:String){
        return fcmRepository.registFCMToken(userId, deviceId, fcmToken)
    }

    suspend fun updateFCMToken(userId:String, deviceId:String, fcmToken:String){
        return fcmRepository.updateFCMToken(userId, deviceId, fcmToken)
    }
    suspend fun deleteFCMToken(userId:String, deviceId:String){
        return fcmRepository.deleteFCMToke(userId, deviceId)
    }
}