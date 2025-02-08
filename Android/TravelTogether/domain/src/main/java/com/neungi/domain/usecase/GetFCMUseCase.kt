package com.neungi.domain.usecase

import com.neungi.domain.repository.FCMRepository
import javax.inject.Inject

class GetFCMUseCase @Inject constructor(
    private val fcmRepository: FCMRepository
) {
    suspend fun registFCMToken(userId:String, deviceId:String, fcmToken:String){
        return fcmRepository.registFCMToken(userId, deviceId, fcmToken)
    }
}