package com.neungi.domain.usecase

import com.neungi.domain.model.ApiResult
import com.neungi.domain.model.Place
import com.neungi.domain.repository.AiPlanningRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecommendPlaceUseCase @Inject constructor(
    private val aiPlanningRepository: AiPlanningRepository
) {

    suspend operator fun invoke(regionNumber:String): Flow<ApiResult<List<Place>>> {
        return aiPlanningRepository.getRecommendPlace(regionNumber)
    }


}