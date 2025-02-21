package com.neungi.data.repository.invite

import com.neungi.data.entity.InviteAcceptEntity
import com.neungi.data.entity.InviteTokenEntity
import okhttp3.RequestBody
import retrofit2.Response

interface InviteRemoteDataSource {

    suspend fun postInviteLink(tripId: Int): Response<InviteTokenEntity>

    suspend fun postInviteAccept(body: RequestBody): Response<InviteAcceptEntity>
}