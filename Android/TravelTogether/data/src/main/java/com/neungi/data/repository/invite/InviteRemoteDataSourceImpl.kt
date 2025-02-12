package com.neungi.data.repository.invite

import com.neungi.data.api.InviteApi
import com.neungi.data.entity.InviteTokenEntity
import retrofit2.Response
import javax.inject.Inject

class InviteRemoteDataSourceImpl @Inject constructor(
    private val inviteApi: InviteApi
) : InviteRemoteDataSource {

    override suspend fun postInviteLink(tripId: Int): Response<InviteTokenEntity> =
        inviteApi.postInviteLink(tripId)

    override suspend fun postInviteAccept(): Response<String> =
        inviteApi.postInviteAccept()
}