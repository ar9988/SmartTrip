package com.neungi.domain.usecase

import com.neungi.domain.model.ApiResult
import com.neungi.domain.repository.FestivalRepository
import javax.inject.Inject

class GetOverviewUseCase @Inject constructor(
    private val festivalRepository: FestivalRepository
) {

    suspend operator fun invoke(contentId:String): ApiResult<String> {
        return festivalRepository.getFestivalOverview(contentId)
    }


}