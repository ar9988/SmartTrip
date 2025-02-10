package com.neungi.data.repository.auth

import com.neungi.data.mapper.TokenMapper
import com.neungi.data.mapper.UserMapper
import com.neungi.domain.model.ApiResult
import com.neungi.domain.model.User
import com.neungi.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authRemoteDataSource: AuthRemoteDataSource
) : AuthRepository {

    override suspend fun postSignUp(photoImage: MultipartBody.Part?, body: RequestBody): ApiResult<User> =
        try {
            val response = withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                authRemoteDataSource.postSignUp(photoImage, body)
            }

            val responseBody = response.body()
            if (response.isSuccessful && (responseBody != null)) {
                ApiResult.success(UserMapper(responseBody))
            } else {
                ApiResult.error(response.errorBody().toString(), null)
            }

        } catch (e: Exception) {
            ApiResult.fail()
        }

    override suspend fun postLogin(body: RequestBody): ApiResult<Pair<User, String>> =
        try {
            val response = withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                authRemoteDataSource.postLogin(body)
            }

            val responseBody = response.body()
            if (response.isSuccessful && (responseBody != null)) {
                ApiResult.success(TokenMapper(responseBody))
            } else {
                ApiResult.error(response.errorBody().toString(), null)
            }

        } catch (e: Exception) {
            ApiResult.fail()
        }

    override suspend fun postSocialLogin(body: RequestBody): ApiResult<Void> =
        try {
            val response = withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                authRemoteDataSource.postSocialLogin(body)
            }

            val responseBody = response.body()
            if (response.isSuccessful && (responseBody != null)) {
                ApiResult.success(responseBody)
            } else {
                ApiResult.error(response.errorBody().toString(), null)
            }

        } catch (e: Exception) {
            ApiResult.fail()
        }
}