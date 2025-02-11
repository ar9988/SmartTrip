package com.neungi.domain.repository

import com.neungi.domain.model.ApiResult
import com.neungi.domain.model.Festival
import kotlinx.coroutines.flow.Flow

interface FestivalRepository {

//    suspend fun getPlaceSearchResult():ApiResult<Place>

    suspend fun getRecommendFestival(startDate:String, endDate:String, regionNumber:String?): Flow<ApiResult<List<Festival>>>

    suspend fun getFestivalOverview(contentId:String):ApiResult<String>

//    suspend fun getRecommendFestivalInfo():ApiResult<Festival>
}