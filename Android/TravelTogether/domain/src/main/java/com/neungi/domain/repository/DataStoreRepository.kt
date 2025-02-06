package com.neungi.domain.repository

import com.neungi.domain.model.LoginInfo
import kotlinx.coroutines.flow.Flow

interface DataStoreRepository {

    suspend fun setJWT(token: String)

    fun getJWT(): Flow<String?>

    suspend fun setUserId(id: String)

    fun getUserId(): Flow<String?>

    suspend fun setUserEmail(email: String)

    fun getUserEmail(): Flow<String?>

    suspend fun setUserName(name: String)

    fun getUserName(): Flow<String?>

    suspend fun setUserProfile(profile: String)

    fun getUserProfile(): Flow<String?>

    fun getLoginInfo():Flow<LoginInfo?>
}