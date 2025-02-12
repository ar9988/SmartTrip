package com.neungi.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface InviteApi {

    @GET("invite")
    fun getInvite(
        @Query("token") token: String
    ): Response<String>

    @POST("invite/{tripId}")
    fun postInviteLink(
        @Path("tripId") tripId: String
    ): Response<String>
}