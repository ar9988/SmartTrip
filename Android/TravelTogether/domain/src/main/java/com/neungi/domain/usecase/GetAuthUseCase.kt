package com.neungi.domain.usecase

import com.neungi.domain.model.ApiResult
import com.neungi.domain.model.User
import com.neungi.domain.repository.AuthRepository
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class GetAuthUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    suspend fun signUp(photoImage: MultipartBody.Part?, body: RequestBody): ApiResult<User> {
        return authRepository.postSignUp(photoImage, body)
    }

    suspend fun login(body: RequestBody): ApiResult<Pair<User, String>> {
        return authRepository.postLogin(body)
    }
}