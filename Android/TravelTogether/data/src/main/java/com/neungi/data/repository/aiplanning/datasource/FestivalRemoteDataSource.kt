package com.neungi.data.repository.aiplanning.datasource

import com.neungi.data.entity.FestivalEntity
import com.neungi.data.entity.FestivalResponse
import retrofit2.Response

interface FestivalRemoteDataSource {

    suspend fun getFestivals(startDate:String,endDate:String,regionCode:String): Response<FestivalResponse>
}