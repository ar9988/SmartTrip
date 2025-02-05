package com.neungi.data.repository.trips

import com.neungi.data.entity.TripEntity
import retrofit2.Response

interface TripsRemoteDataSource {
    suspend fun getTrips(userId: String): Response<List<TripEntity>>
}