package com.neungi.data.repository.trips

import com.neungi.data.entity.TripsResponse
import com.neungi.domain.model.ApiResult
import com.neungi.domain.model.Trip
import okhttp3.RequestBody
import retrofit2.Response
import java.time.LocalDate

interface TripsRemoteDataSource {
    suspend fun getTrips(userId: String): Response<TripsResponse>
    suspend fun createTrip(body: RequestBody): Response<Boolean>
    suspend fun deleteTrip(userId: String,tripId:Int) : Response<Boolean>
    suspend fun getLatestTrip():Response<TripsResponse>
}