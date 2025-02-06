package com.neungi.data.api

import com.neungi.data.entity.TokenEntity
import com.neungi.data.entity.UserEntity
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/signup")
    suspend fun postSignUp(
        @Body body: RequestBody
    ): Response<UserEntity>

    @POST("auth/login")
    suspend fun postLogin(
        @Body body: RequestBody
    ): Response<TokenEntity>

    @POST("auth/social/login")
    suspend fun postSocialLogin(
        @Body body: RequestBody
    ): Response<Void>
}