package com.neungi.data.api


import com.neungi.data.entity.FestivalResponse
import com.neungi.data.entity.OverViewEntity
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface FestivalApi {
    @GET("festivals")
    suspend fun getFestivals(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("regionCode") regionCode: String? = null
    ): Response<FestivalResponse>

    @GET("festivals/overview")
    suspend fun getFestivalOverView(
        @Query("contentid") contentid: String
    ): Response<OverViewEntity>
}