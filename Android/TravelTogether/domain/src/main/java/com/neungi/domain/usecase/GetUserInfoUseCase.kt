package com.neungi.domain.usecase

import com.neungi.domain.model.LoginInfo
import com.neungi.domain.repository.DataStoreRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserInfoUseCase @Inject constructor(
    private val dataStoreRepository: DataStoreRepository
) {

    fun getJWT(): Flow<String?> = dataStoreRepository.getJWT()

    fun getUserId(): Flow<String?> = dataStoreRepository.getUserId()

    fun getUserEmail(): Flow<String?> = dataStoreRepository.getUserEmail()

    fun getUserName(): Flow<String?> = dataStoreRepository.getUserName()

    fun getUserProfileMessage(): Flow<String?> = dataStoreRepository.getUserProfileMessage()

    fun getUserProfile(): Flow<String?> = dataStoreRepository.getUserProfile()

    fun getLoginInfo():Flow<LoginInfo?> = dataStoreRepository.getLoginInfo()

    fun getDeviceId():Flow<String?> = dataStoreRepository.getDeviceId()
}