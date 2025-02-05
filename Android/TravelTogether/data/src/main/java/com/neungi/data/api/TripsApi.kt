package com.neungi.data.api

import com.neungi.data.entity.TripEntity
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface TripsApi {
    @GET("trips/{userId}")
    suspend fun getTrips(
        @Path("userId") userId: String
    ): Response<List<TripEntity>>


}