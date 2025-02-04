package com.neungi.domain.repository

import com.neungi.domain.model.ApiResult
import com.neungi.domain.model.Festival

interface FestivalRepository {

//    suspend fun getPlaceSearchResult():ApiResult<Place>

    suspend fun getRecommendFestival(startDate:String, endDate:String, regionNumber:String):ApiResult<List<Festival>>

//    suspend fun getRecommendFestivalInfo():ApiResult<Festival>
}