package com.neungi.domain.usecase


import com.neungi.domain.model.ApiResult
import com.neungi.domain.model.Festival
import com.neungi.domain.repository.FestivalRepository
import javax.inject.Inject

class GetRecommendFestivalUseCase @Inject constructor(
    private val festivalRepository: FestivalRepository
) {

    suspend operator fun invoke(startDate:String, endDate:String, regionCode:String?): ApiResult<List<Festival>> {
//        val regionCode = regionMapper.getRegionCode(regionName)

        return festivalRepository.getRecommendFestival(startDate, endDate, regionCode)
    }


}