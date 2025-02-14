package com.neungi.domain.usecase

import com.neungi.domain.model.ApiResult
import com.neungi.domain.model.Trip
import com.neungi.domain.repository.TripsRepository
import java.time.LocalDate
import javax.inject.Inject

class GetTripUseCase @Inject constructor(
    private val tripsRepository : TripsRepository
){
    suspend fun getTrips(userId: String): ApiResult<List<Trip>> {
        return tripsRepository.getTrips(userId)
    }

    suspend fun makeTrip(userId: String,title:String,startDate: LocalDate,endDate: LocalDate) : ApiResult<Boolean> {
        println("create Trip In UseCase")
        return tripsRepository.createTrip(userId,title,startDate,endDate)
    }

    suspend fun removeTrip(userId: String, trip: Int): ApiResult<Boolean>{
        return tripsRepository.deleteTrip(userId,trip)
    }
}