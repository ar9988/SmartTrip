package com.neungi.data.api

import com.neungi.data.entity.InviteTokenEntity
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Path

interface InviteApi {

    @POST("invite/{tripId}")
    suspend fun postInviteLink(
        @Path("tripId") tripId: Int
    ): Response<InviteTokenEntity>

    @POST("invite/accept")
    suspend fun postInviteAccept(): Response<String>
}