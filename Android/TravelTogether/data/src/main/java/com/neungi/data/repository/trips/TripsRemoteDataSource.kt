package com.neungi.data.repository.trips

import com.neungi.data.entity.TripsResponse
import okhttp3.RequestBody
import retrofit2.Response
import java.time.LocalDate

interface TripsRemoteDataSource {
    suspend fun getTrips(userId: String): Response<TripsResponse>
    suspend fun createTrip(body: RequestBody):Response<TripsResponse>
}