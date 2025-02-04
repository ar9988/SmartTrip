package com.neungi.data.repository.auth

import com.neungi.data.entity.TokenEntity
import com.neungi.data.entity.UserEntity
import okhttp3.RequestBody
import retrofit2.Response

interface AuthRemoteDataSource {

    suspend fun postSignUp(body: RequestBody): Response<UserEntity>

    suspend fun postLogin(body: RequestBody): Response<TokenEntity>

    suspend fun postSocialLogin(body: RequestBody): Response<Void>
}