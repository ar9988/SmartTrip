package com.neungi.data.api

import TripEntity
import com.neungi.data.entity.TripsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface TripsApi {
    @GET("trips/{id}")
    suspend fun getTrips(
        @Path("id") userId: String
    ): Response<TripsResponse>


}