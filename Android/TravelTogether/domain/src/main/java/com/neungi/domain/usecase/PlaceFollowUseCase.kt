package com.neungi.domain.usecase

import com.neungi.domain.model.ApiResult
import com.neungi.domain.repository.AiPlanningRepository
import com.neungi.domain.repository.FestivalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PlaceFollowUseCase @Inject constructor(
    private val aiPlanningRepository: AiPlanningRepository
) {

    suspend operator fun invoke(contentId:String): Flow<ApiResult<Boolean>> {
        return aiPlanningRepository.follow(contentId)
    }
}