package com.neungi.data.repository.festival

import android.util.Log
import com.neungi.data.mapper.FestivalMapper
import com.neungi.data.repository.festival.datasource.FestivalRemoteDataSource
import com.neungi.domain.model.ApiResult
import com.neungi.domain.model.Festival
import com.neungi.domain.repository.FestivalRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FestivalRepositoryImpl@Inject constructor(
    private val festivalRemoteDataSource: FestivalRemoteDataSource
) : FestivalRepository {
//    override suspend fun getPlaceSearchResult(): ApiResult<Place> {
//        try {
//            val response = withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
//                albumsRemoteDataSource.getAlbums()
//            }
//
//            val body = response.body()
//            if (response.isSuccessful && (body != null)) {
//                ApiResult.success(AlbumsMapper(body))
//            } else {
//                ApiResult.error(response.errorBody().toString(), null)
//            }
//
//        } catch (e: Exception) {
//            ApiResult.fail()
//        }
//    }

    override suspend fun getRecommendFestival(
        startDate: String,
        endDate: String,
        regionNumber: String?
    ): Flow<ApiResult<List<Festival>>> = flow {
        emit(ApiResult.loading(null))
        try {
            val response = withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                Log.d("regionCode", regionNumber.toString())
                festivalRemoteDataSource.getFestivals(startDate, endDate, regionNumber)
            }

            val body = response.body()
            if (response.isSuccessful && (body != null)) {
                emit(ApiResult.success(FestivalMapper(body.festivals)))
            } else {
                emit(ApiResult.error(response.errorBody().toString(), null))
            }

        } catch (e: Exception) {
            emit(ApiResult.fail())
        }
    }

    override suspend fun getFestivalOverview(
        contentId: String
    ): ApiResult<String> =
        try {
            val response = withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                festivalRemoteDataSource.getFestivalOverview(contentId)
            }
            val body = response.body()
            if (response.isSuccessful && (body != null)) {
                ApiResult.success(body.overview)
            } else {
                ApiResult.error(response.errorBody().toString(), null)
            }

        } catch (e: Exception) {
            ApiResult.fail()
        }

}