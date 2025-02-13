package com.neungi.data.api

import com.neungi.data.entity.TripsResponse
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface TripsApi {
    @GET("trips/{id}")
    suspend fun getTrips(
        @Path("id") userId: String
    ): Response<TripsResponse>


    @POST("trips/")
    suspend fun createTrip(
        @Body body: RequestBody
    ): Response<TripsResponse>

    @DELETE("trips/{}")
    suspend fun deleteTrip(userId: String, tripId: Int): Response<Boolean>

}