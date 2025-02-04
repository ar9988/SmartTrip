package com.neungi.domain.repository

import com.neungi.domain.model.ApiResult
import com.neungi.domain.model.User
import okhttp3.RequestBody

interface AuthRepository {

    suspend fun postSignUp(body: RequestBody): ApiResult<User>

    suspend fun postLogin(body: RequestBody): ApiResult<Pair<User, String>>

    suspend fun postSocialLogin(body: RequestBody): ApiResult<Void>
}