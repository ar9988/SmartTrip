package com.neungi.domain.usecase

import com.neungi.domain.model.ApiResult
import com.neungi.domain.model.Trip
import com.neungi.domain.repository.TripsRepository
import javax.inject.Inject

class GetTripUseCase @Inject constructor(
    private val tripsRepository : TripsRepository
){
    suspend fun getTrips(userId: String): ApiResult<List<Trip>> {
        return tripsRepository.getTrips(userId)
    }
}