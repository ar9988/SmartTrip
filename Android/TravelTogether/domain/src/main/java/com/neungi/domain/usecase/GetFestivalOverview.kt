package com.neungi.domain.usecase

import com.neungi.domain.model.ApiResult
import com.neungi.domain.model.Festival
import com.neungi.domain.repository.FestivalRepository
import javax.inject.Inject

class GetFestivalOverview @Inject constructor(
    private val aiPlanningRepository: FestivalRepository
) {

    suspend operator fun invoke(contentId:String): ApiResult<String> {
        return aiPlanningRepository.getFestivalOverview(contentId)
    }


}