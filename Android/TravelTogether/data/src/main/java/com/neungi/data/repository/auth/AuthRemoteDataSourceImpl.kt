package com.neungi.data.repository.auth

import com.neungi.data.api.AuthApi
import com.neungi.data.entity.TokenEntity
import com.neungi.data.entity.UserEntity
import okhttp3.RequestBody
import retrofit2.Response
import javax.inject.Inject

class AuthRemoteDataSourceImpl @Inject constructor(
    private val authApi: AuthApi
) : AuthRemoteDataSource {

    override suspend fun postSignUp(body: RequestBody): Response<UserEntity> =
        authApi.postSignUp(body)

    override suspend fun postLogin(body: RequestBody): Response<TokenEntity> =
        authApi.postLogin(body)

    override suspend fun postSocialLogin(body: RequestBody): Response<Void> =
        authApi.postSocialLogin(body)
}