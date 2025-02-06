package com.neungi.data.repository.aiplanning.datasource

import com.neungi.data.entity.SearchPlaceResponse
import retrofit2.Response

interface AiPlanningDataSource {
    suspend fun getSearchPlaces(keyword:String): Response<SearchPlaceResponse>
}