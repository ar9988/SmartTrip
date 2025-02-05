package com.neungi.data.repository.trips

import com.neungi.data.mapper.AlbumsMapper
import com.neungi.data.mapper.TripMapper
import com.neungi.domain.model.ApiResult
import com.neungi.domain.model.Trip
import com.neungi.domain.model.TripMember
import com.neungi.domain.repository.TripsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TripsRepositoryImpl @Inject constructor(
    private val tripsRemoteDataSource: TripsRemoteDataSource
) : TripsRepository {
    override suspend fun getTrips(userId:String): ApiResult<List<Trip>> =
        try {
            val response = withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                tripsRemoteDataSource.getTrips(userId)
            }

            val body = response.body()
            if (response.isSuccessful && (body != null)) {
                ApiResult.success(TripMapper(body))
            } else {
                ApiResult.error(response.errorBody().toString(), null)
            }

        } catch (e: Exception) {
            ApiResult.fail()
        }

}