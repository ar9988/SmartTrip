package com.neungi.data.api


import com.neungi.data.entity.FestivalEntity
import com.neungi.data.entity.FestivalResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface FestivalApi {
    @GET("festivals")
    suspend fun getFestivals(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("regionCode") regionCode: String
    ): Response<FestivalResponse>
}