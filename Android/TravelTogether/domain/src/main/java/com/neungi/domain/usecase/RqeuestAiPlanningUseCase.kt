package com.neungi.domain.usecase

import com.neungi.domain.model.AiPlanningRequest
import com.neungi.domain.model.ApiResult
import com.neungi.domain.model.Festival
import com.neungi.domain.repository.AiPlanningRepository
import com.neungi.domain.repository.FestivalRepository
import okhttp3.RequestBody
import retrofit2.http.Body
import javax.inject.Inject

class RqeuestAiPlanningUseCase @Inject constructor(
    private val aiPlanningRepository: AiPlanningRepository
) {

    suspend operator fun invoke(requestBody: AiPlanningRequest): ApiResult<Boolean> {
//        val regionCode = regionMapper.getRegionCode(regionName)

        return aiPlanningRepository.requestAiPlanning(requestBody)
    }


}