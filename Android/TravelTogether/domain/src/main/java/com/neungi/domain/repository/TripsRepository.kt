package com.neungi.domain.repository

import com.neungi.domain.model.ApiResult
import com.neungi.domain.model.Trip
import okhttp3.Response
import java.time.LocalDate

interface TripsRepository {
    suspend fun getTrips(userId: String): ApiResult<List<Trip>>
    suspend fun deleteTrip(userId: String, trip: Trip): ApiResult<Boolean>
    suspend fun createTrip(userId:String,startDate: LocalDate,endDate: LocalDate) : ApiResult<Response>
}