package com.neungi.data.repository.aiplanning.datasource

import com.neungi.data.entity.SearchPlaceResponse
import com.neungi.domain.model.AiPlanningRequest
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body

interface AiPlanningDataSource {
    suspend fun getSearchPlaces(keyword:String): Response<SearchPlaceResponse>
    suspend fun requestAiPlanning(requestBody: AiPlanningRequest):Response<Boolean>
}