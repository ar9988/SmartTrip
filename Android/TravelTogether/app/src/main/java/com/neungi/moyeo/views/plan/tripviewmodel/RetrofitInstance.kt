package com.neungi.moyeo.views.plan.tripviewmodel

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "http://43.202.51.112:8080/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // JSON을 객체로 변환
            .build()
            .create(ApiService::class.java) // ApiService 인터페이스 구현
    }
}
