package com.neungi.data.repository.trips

import com.neungi.domain.model.ApiResult
import com.neungi.domain.model.Trip
import com.neungi.domain.repository.TripsRepository
import javax.inject.Inject

class TripsRepositoryImpl @Inject constructor(
    private val tripsRemoteDataSource: TripsRemoteDataSource
) : TripsRepository {
    override suspend fun getTrips(): ApiResult<List<Trip>> {
        TODO("Not yet implemented")
    }

}