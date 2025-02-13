package com.neungi.data.repository.aiplanning.datasource

import com.neungi.data.api.AiPlanningApi
import com.neungi.data.entity.RecommendPlaceResponse
import com.neungi.data.entity.SearchPlaceResponse
import com.neungi.domain.model.AiPlanningRequest
import com.neungi.domain.model.Place
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import javax.inject.Inject

class AiPlanningDataSourceImpl @Inject constructor(
    private val aiPlanningApi : AiPlanningApi
) : AiPlanningDataSource {
    override suspend fun getSearchPlaces(keyword: String): Response<SearchPlaceResponse> = aiPlanningApi.getSearchPlaces(keyword)
    override suspend fun requestAiPlanning(requestBody: AiPlanningRequest): Response<Boolean> = aiPlanningApi.requestAiPlanning(requestBody)
    override suspend fun getRecommendPlace(regionNumber: Int): Response<List<RecommendPlaceResponse>> = aiPlanningApi.getRecommendPlace(regionNumber)
    override suspend fun follow(contentId: Int): Response<Boolean>  = aiPlanningApi.follow(contentId)
    override suspend fun getFollowedPlaces(): Response<List<RecommendPlaceResponse>> = aiPlanningApi.getFavoritePlaces()

}