package com.neungi.data.api

import com.neungi.data.entity.TokenEntity
import com.neungi.data.entity.UserEntity
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part

interface AuthApi {

    @Multipart
    @POST("auth/signup")
    suspend fun postSignUp(
        @Part files: MultipartBody.Part?,
        @Part("signup_data") body: RequestBody
    ): Response<UserEntity>

    @POST("auth/login")
    suspend fun postLogin(
        @Body body: RequestBody
    ): Response<TokenEntity>

    @Multipart
    @PATCH("auth/profile")
    suspend fun patchProfile(
        @Part files: MultipartBody.Part?,
        @Part("profile_data") body: RequestBody
    ): Response<UserEntity>

    @POST("auth/social/login")
    suspend fun postSocialLogin(
        @Body body: RequestBody
    ): Response<Void>
}