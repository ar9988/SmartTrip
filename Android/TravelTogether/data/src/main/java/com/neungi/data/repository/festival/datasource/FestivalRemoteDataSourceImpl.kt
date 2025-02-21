package com.neungi.data.repository.festival.datasource

import com.neungi.data.api.FestivalApi
import com.neungi.data.entity.FestivalResponse
import com.neungi.data.entity.OverViewEntity
import retrofit2.Response
import javax.inject.Inject

class FestivalRemoteDataSourceImpl @Inject constructor(
    private val festivalApi: FestivalApi
) : FestivalRemoteDataSource {
    override suspend fun getFestivals(
        startDate: String,
        endDate: String,
        regionCode: String?
    ): Response<FestivalResponse> = festivalApi.getFestivals(startDate,endDate,regionCode)

    override suspend fun getFestivalOverview(contentid: String): Response<OverViewEntity> = festivalApi.getFestivalOverView(contentid)
}