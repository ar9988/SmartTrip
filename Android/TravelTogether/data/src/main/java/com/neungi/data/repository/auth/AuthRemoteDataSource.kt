package com.neungi.data.repository.auth

import com.neungi.data.entity.TokenEntity
import com.neungi.data.entity.UserEntity
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

interface AuthRemoteDataSource {

    suspend fun postSignUp(photoImage: MultipartBody.Part?, body: RequestBody): Response<UserEntity>

    suspend fun postLogin(body: RequestBody): Response<TokenEntity>

    suspend fun patchProfile(photoImage: MultipartBody.Part?, body: RequestBody): Response<UserEntity>

    suspend fun postSocialLogin(body: RequestBody): Response<Void>
}