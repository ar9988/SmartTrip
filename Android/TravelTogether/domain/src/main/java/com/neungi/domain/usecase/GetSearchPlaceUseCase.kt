package com.neungi.domain.usecase

import com.neungi.domain.model.ApiResult
import com.neungi.domain.model.Place
import com.neungi.domain.repository.AiPlanningRepository
import javax.inject.Inject

class GetSearchPlaceUseCase @Inject constructor(
    private val aiPlanningRepository: AiPlanningRepository
) {
    suspend fun getSearchPlace(keyword:String) : ApiResult<List<Place>> {
        return aiPlanningRepository.getSearchPlaces(keyword)
    }
}