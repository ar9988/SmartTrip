package com.neungi.data.api

import com.neungi.data.entity.InviteAcceptEntity
import com.neungi.data.entity.InviteTokenEntity
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface InviteApi {

    @POST("invite/{tripId}")
    suspend fun postInviteLink(
        @Path("tripId") tripId: Int
    ): Response<InviteTokenEntity>

    @POST("invite/accept")
    suspend fun postInviteAccept(
        @Body body: RequestBody
    ): Response<InviteAcceptEntity>
}