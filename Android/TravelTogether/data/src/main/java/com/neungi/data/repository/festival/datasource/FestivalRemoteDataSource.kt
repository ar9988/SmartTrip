package com.neungi.data.repository.festival.datasource

import com.neungi.data.entity.FestivalResponse
import com.neungi.data.entity.OverViewEntity
import retrofit2.Response

interface FestivalRemoteDataSource {

    suspend fun getFestivals(startDate:String,endDate:String,regionCode:String): Response<FestivalResponse>

    suspend fun getFestivalOverview(contentid:String):Response<OverViewEntity>
}