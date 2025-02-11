package com.neungi.data.repository.trips

import TripEntity
import com.neungi.data.entity.TripsResponse
import retrofit2.Response

interface TripsRemoteDataSource {
    suspend fun getTrips(userId: String): Response<TripsResponse>
}