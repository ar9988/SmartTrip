package com.neungi.data.repository.aiplanning.datasource

import com.neungi.data.api.AiPlanningApi
import com.neungi.data.entity.SearchPlaceResponse
import retrofit2.Response
import javax.inject.Inject

class AiPlanningDataSourceImpl @Inject constructor(
    private val aiPlanningApi : AiPlanningApi
) : AiPlanningDataSource {
    override suspend fun getSearchPlaces(keyword: String): Response<SearchPlaceResponse> = aiPlanningApi.getSearchPlaces(keyword)

}