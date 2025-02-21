package com.neungi.domain.usecase

import com.neungi.domain.repository.DataStoreRepository
import javax.inject.Inject

class SetUserInfoUseCase @Inject constructor(
    private val dataStoreRepository: DataStoreRepository
) {

    suspend fun setJWT(token: String) = dataStoreRepository.setJWT(token)

    suspend fun setUserId(id: String) = dataStoreRepository.setUserId(id)

    suspend fun setUserEmail(email: String) = dataStoreRepository.setUserEmail(email)

    suspend fun setUserName(name: String) = dataStoreRepository.setUserName(name)

    suspend fun setUserProfileMessage(message: String) =
        dataStoreRepository.setUserProfileMessage(message)

    suspend fun setUserProfile(profile: String) = dataStoreRepository.setUserProfile(profile)

    suspend fun logOut() = dataStoreRepository.logout()

    suspend fun setDeviceInfo(deviceID:String) = dataStoreRepository.setDeviceId(deviceID)
}