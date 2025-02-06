package com.neungi.data.api

import com.neungi.data.entity.FestivalResponse
import com.neungi.data.entity.SearchPlaceResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface AiPlanningApi {
    @GET("search/kakao")
    suspend fun getSearchPlaces(
        @Query("keyword") keyword: String
    ): Response<SearchPlaceResponse>
}