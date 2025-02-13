package com.neungi.data.repository.aiplanning.datasource

import com.neungi.data.entity.RecommendPlaceResponse
import com.neungi.data.entity.SearchPlaceResponse
import com.neungi.domain.model.AiPlanningRequest
import com.neungi.domain.model.Place
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body

interface AiPlanningDataSource {
    suspend fun getSearchPlaces(keyword:String): Response<SearchPlaceResponse>
    suspend fun requestAiPlanning(requestBody: AiPlanningRequest):Response<Boolean>
    suspend fun getRecommendPlace(regionNumber:Int):Response<List<RecommendPlaceResponse>>
    suspend fun follow(contentId : Int):Response<Boolean>
    suspend fun getFollowedPlaces():Response<List<RecommendPlaceResponse>>
}