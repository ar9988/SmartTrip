package com.neungi.domain.usecase

import com.neungi.domain.model.ApiResult
import com.neungi.domain.model.Trip
import com.neungi.domain.repository.TripsRepository
import okhttp3.Response
import java.time.LocalDate
import javax.inject.Inject

class GetTripUseCase @Inject constructor(
    private val tripsRepository : TripsRepository
){
    suspend fun getTrips(userId: String): ApiResult<List<Trip>> {
        return tripsRepository.getTrips(userId)
    }

    suspend fun makeTrip(userId: String,title:String,startDate: LocalDate,endDate: LocalDate) : ApiResult<List<Trip>> {
        return tripsRepository.createTrip(userId,title,startDate,endDate)
    }

    suspend fun removeTrip(userId: String, trip: Trip): ApiResult<Boolean>{
        return tripsRepository.deleteTrip(userId,trip)
    }
}