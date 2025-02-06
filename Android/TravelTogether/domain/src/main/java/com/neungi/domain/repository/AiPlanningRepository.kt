package com.neungi.domain.repository

import com.neungi.domain.model.ApiResult
import com.neungi.domain.model.Place

interface AiPlanningRepository {

    suspend fun getSearchPlaces(keyword:String):ApiResult<List<Place>>
}