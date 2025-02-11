package com.neungi.data.api

import com.neungi.data.entity.FestivalResponse
import com.neungi.data.entity.SearchPlaceResponse
import com.neungi.domain.model.AiPlanningRequest
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AiPlanningApi {
    @GET("search/kakao")
    suspend fun getSearchPlaces(
        @Query("keyword") keyword: String
    ): Response<SearchPlaceResponse>

    @POST("ai/generate")
    suspend fun requestAiPlanning(
        @Body requestBody: AiPlanningRequest
    ): Response<Boolean>
}