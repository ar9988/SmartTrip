package com.neungi.data.repository.aiplanning.datasource

import com.neungi.data.api.AiPlanningApi
import com.neungi.data.entity.SearchPlaceResponse
import com.neungi.domain.model.AiPlanningRequest
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import javax.inject.Inject

class AiPlanningDataSourceImpl @Inject constructor(
    private val aiPlanningApi : AiPlanningApi
) : AiPlanningDataSource {
    override suspend fun getSearchPlaces(keyword: String): Response<SearchPlaceResponse> = aiPlanningApi.getSearchPlaces(keyword)
    override suspend fun requestAiPlanning(requestBody: AiPlanningRequest): Response<Boolean> = aiPlanningApi.requestAiPlanning(requestBody)

}