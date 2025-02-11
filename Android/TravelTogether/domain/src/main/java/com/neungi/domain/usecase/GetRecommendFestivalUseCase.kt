package com.neungi.domain.usecase


import com.neungi.domain.model.ApiResult
import com.neungi.domain.model.Festival
import com.neungi.domain.repository.FestivalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecommendFestivalUseCase @Inject constructor(
    private val festivalRepository: FestivalRepository
) {

    suspend operator fun invoke(startDate:String, endDate:String, regionCode:String?): Flow<ApiResult<List<Festival>>> {
//        val regionCode = regionMapper.getRegionCode(regionName)

        return festivalRepository.getRecommendFestival(startDate, endDate, regionCode)
    }


}