package com.neungi.data.repository.aiplanning

import com.neungi.data.mapper.FestivalMapper
import com.neungi.data.mapper.PlaceMapper
import com.neungi.data.repository.aiplanning.datasource.AiPlanningDataSource
import com.neungi.domain.model.AiPlanningRequest
import com.neungi.domain.model.ApiResult
import com.neungi.domain.model.Place
import com.neungi.domain.repository.AiPlanningRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.RequestBody
import retrofit2.http.Body
import javax.inject.Inject

class AiPlanningRepositoryImpl @Inject constructor(
    private val aiPlanningDataSource: AiPlanningDataSource
) : AiPlanningRepository {
    override suspend fun getSearchPlaces(keyword: String): ApiResult<List<Place>>  =
        try {
            val response = withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                aiPlanningDataSource.getSearchPlaces(keyword)
            }

            val body = response.body()
            if (response.isSuccessful && (body != null)) {
                ApiResult.success(PlaceMapper(body.places))
            } else {
                ApiResult.error(response.errorBody().toString(), null)
            }

        } catch (e: Exception) {
            ApiResult.fail()
        }

    override suspend fun requestAiPlanning(requestBody: AiPlanningRequest): ApiResult<Boolean> =
        try{
            val response = withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                aiPlanningDataSource.requestAiPlanning(requestBody)
            }

            val body = response.body()
            if (response.isSuccessful && (body != null)) {
                ApiResult.success(body)
            } else {
                ApiResult.error(response.errorBody().toString(), false)
            }
        } catch (e: Exception) {
            ApiResult.fail()
        }
    }
