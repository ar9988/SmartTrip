package com.neungi.data.util

import android.util.Log
import com.neungi.domain.repository.DataStoreRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JwtInterceptor @Inject constructor(
    private val dataStoreRepository: DataStoreRepository
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = runBlocking { dataStoreRepository.getJWT().first() }
        val responseWithJwt = if (token.isNullOrBlank()) {
            originalRequest
        } else {
            Log.d("JwtInterceptor", "intercept: $token")
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        }

        return chain.proceed(responseWithJwt)
    }
}