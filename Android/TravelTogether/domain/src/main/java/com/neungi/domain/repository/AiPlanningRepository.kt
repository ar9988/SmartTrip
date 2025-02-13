package com.neungi.domain.repository

import com.neungi.domain.model.AiPlanningRequest
import com.neungi.domain.model.ApiResult
import com.neungi.domain.model.Place
import kotlinx.coroutines.flow.Flow
import okhttp3.RequestBody
import retrofit2.http.Body

interface AiPlanningRepository {

    suspend fun getSearchPlaces(keyword:String):ApiResult<List<Place>>

    suspend fun requestAiPlanning(requestBody: AiPlanningRequest): ApiResult<Boolean>

    suspend fun getRecommendPlace(regionNumber:String): Flow<ApiResult<List<Place>>>

    suspend fun follow(contentId :String) : Flow<ApiResult<Boolean>>

    suspend fun getFollowedPlaces(): Flow<ApiResult<List<Place>>>
}