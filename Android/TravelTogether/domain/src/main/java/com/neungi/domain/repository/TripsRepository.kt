package com.neungi.domain.repository

import com.neungi.domain.model.ApiResult
import com.neungi.domain.model.Trip

interface TripsRepository {
    suspend fun getTrips(userId: String): ApiResult<List<Trip>>
}