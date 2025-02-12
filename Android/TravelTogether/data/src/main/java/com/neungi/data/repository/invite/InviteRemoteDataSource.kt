package com.neungi.data.repository.invite

import com.neungi.data.entity.InviteTokenEntity
import retrofit2.Response

interface InviteRemoteDataSource {

    suspend fun postInviteLink(tripId: Int): Response<InviteTokenEntity>

    suspend fun postInviteAccept(): Response<String>
}